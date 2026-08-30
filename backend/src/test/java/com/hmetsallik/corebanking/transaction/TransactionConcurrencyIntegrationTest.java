package com.hmetsallik.corebanking.transaction;

import com.hmetsallik.corebanking.AbstractIntegrationTest;
import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.dto.CreateAccountRequest;
import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.common.dto.Money;
import com.hmetsallik.corebanking.transaction.dto.CreateTransactionRequest;
import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class TransactionConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void concurrentWithdrawals_doNotOverdrawAccount() throws InterruptedException {
        UUID accountId = createAccountWithEurBalance();
        deposit(accountId, BigDecimal.valueOf(100));

        int threadCount = 10;
        BigDecimal withdrawalAmount = BigDecimal.valueOf(20);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        AtomicInteger otherCount = new AtomicInteger();

        try (ExecutorService pool = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    try {
                        startGate.await();
                        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                                "/accounts/" + accountId + "/transactions",
                                transactionRequest(withdrawalAmount, Direction.OUT),
                                TransactionResponse.class);

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            successCount.incrementAndGet();
                        } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                            conflictCount.incrementAndGet();
                        } else {
                            otherCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        otherCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startGate.countDown();
            boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
        }

        assertThat(otherCount.get()).isZero();
        // 100 / 20 = exactly 5 withdrawals can succeed; the other 5 must be rejected as insufficient funds
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(conflictCount.get()).isEqualTo(5);

        AccountResponse finalAccount =
                restTemplate.getForEntity("/accounts/" + accountId, AccountResponse.class).getBody();
        assertThat(finalAccount).isNotNull();
        BigDecimal finalBalance = finalAccount.getBalances().getFirst().getMoney().getAmount();

        // If the row lock ever failed, a lost update would leave this negative or wrong instead of exactly zero
        assertThat(finalBalance).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private UUID createAccountWithEurBalance() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-" + UUID.randomUUID());
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR));

        AccountResponse response = restTemplate.postForEntity("/accounts", request, AccountResponse.class).getBody();
        assertThat(response).isNotNull();
        return response.getAccountId();
    }

    private void deposit(UUID accountId, BigDecimal amount) {
        restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions",
                transactionRequest(amount, Direction.IN),
                TransactionResponse.class);
    }

    private CreateTransactionRequest transactionRequest(BigDecimal amount, Direction direction) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setMoney(new Money(amount, Currency.EUR));
        request.setDirection(direction);
        request.setDescription("concurrency test withdrawal");
        return request;
    }
}