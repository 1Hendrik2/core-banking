package com.hmetsallik.corebanking.account;

import com.hmetsallik.corebanking.AbstractIntegrationTest;
import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.dto.CreateAccountRequest;
import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.dto.ErrorResponse;
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
class AccountApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAccount_returns201WithZeroedBalances() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-" + UUID.randomUUID());
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR, Currency.USD));

        ResponseEntity<AccountResponse> response =
                restTemplate.postForEntity("/accounts", request, AccountResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountId()).isNotNull();
        assertThat(response.getBody().getBalances()).hasSize(2);
        response.getBody().getBalances().forEach(balance ->
                assertThat(balance.getMoney().getAmount()).isEqualByComparingTo(BigDecimal.ZERO));
    }

    @Test
    void createAccount_duplicateCurrencies_returns400() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-" + UUID.randomUUID());
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR, Currency.EUR));

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/accounts", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createAccount_missingFields_returns400WithFieldErrors() {
        CreateAccountRequest request = new CreateAccountRequest();

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/accounts", request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFieldErrors()).isNotEmpty();
    }

    @Test
    void getAccount_existingAccount_returns200WithBalances() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-" + UUID.randomUUID());
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR));

        AccountResponse created = restTemplate.postForEntity("/accounts", request, AccountResponse.class).getBody();
        assertThat(created).isNotNull();

        ResponseEntity<AccountResponse> response =
                restTemplate.getForEntity("/accounts/" + created.getAccountId(), AccountResponse.class);
        AccountResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();
        assertThat(body.getAccountId()).isEqualTo(created.getAccountId());
    }

    @Test
    void getAccount_nonExistentAccount_returns404() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity("/accounts/" + UUID.randomUUID(), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
