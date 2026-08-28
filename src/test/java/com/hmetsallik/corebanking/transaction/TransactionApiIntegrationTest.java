package com.hmetsallik.corebanking.transaction;

import com.hmetsallik.corebanking.AbstractIntegrationTest;
import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.dto.CreateAccountRequest;
import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.common.dto.ErrorResponse;
import com.hmetsallik.corebanking.common.dto.Money;
import com.hmetsallik.corebanking.transaction.dto.CreateTransactionRequest;
import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class TransactionApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID accountId;

    @BeforeEach
    void createAccountWithEurBalance() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-" + UUID.randomUUID());
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR));

        AccountResponse account = restTemplate.postForEntity("/accounts", request, AccountResponse.class).getBody();
        assertThat(account).isNotNull();
        accountId = account.getAccountId();
    }

    @Test
    void createTransaction_deposit_returns201AndIncreasesBalance() {
        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.valueOf(100), Currency.EUR, Direction.IN), TransactionResponse.class);
        TransactionResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(body).isNotNull();
        assertThat(body.getBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void createTransaction_withdrawalAfterDeposit_returns201AndDecreasesBalance() {
        restTemplate.postForEntity("/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.valueOf(100), Currency.EUR, Direction.IN), TransactionResponse.class);

        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.valueOf(30), Currency.EUR, Direction.OUT), TransactionResponse.class);
        TransactionResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(body).isNotNull();
        assertThat(body.getBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(70));
    }

    @Test
    void createTransaction_accountDoesNotExist_returns404() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/accounts/" + UUID.randomUUID() + "/transactions",
                requestOf(BigDecimal.TEN, Currency.EUR, Direction.IN), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createTransaction_zeroAmount_returns400() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.ZERO, Currency.EUR, Direction.IN), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTransaction_currencyNotHeldByAccount_returns400() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.TEN, Currency.USD, Direction.IN), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTransaction_withdrawalExceedsBalance_returns409() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.valueOf(50), Currency.EUR, Direction.OUT), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getTransactions_returnsCreatedTransactions() {
        restTemplate.postForEntity("/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.valueOf(20), Currency.EUR, Direction.IN), TransactionResponse.class);
        restTemplate.postForEntity("/accounts/" + accountId + "/transactions",
                requestOf(BigDecimal.valueOf(5), Currency.EUR, Direction.OUT), TransactionResponse.class);

        ResponseEntity<TransactionResponse[]> response =
                restTemplate.getForEntity("/accounts/" + accountId + "/transactions", TransactionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    private CreateTransactionRequest requestOf(BigDecimal amount, Currency currency, Direction direction) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setMoney(new Money(amount, currency));
        request.setDirection(direction);
        request.setDescription("integration test transaction");
        return request;
    }
}