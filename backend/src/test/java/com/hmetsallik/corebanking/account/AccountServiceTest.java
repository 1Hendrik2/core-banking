package com.hmetsallik.corebanking.account;

import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.dto.CreateAccountRequest;
import com.hmetsallik.corebanking.account.event.AccountCreatedEvent;
import com.hmetsallik.corebanking.account.exception.AccountNotFoundException;
import com.hmetsallik.corebanking.account.exception.DuplicateCurrencyException;
import com.hmetsallik.corebanking.common.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_duplicateCurrencies_throwsDuplicateCurrencyException_andWritesNothing() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-001");
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR, Currency.EUR));

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(DuplicateCurrencyException.class);

        verifyNoInteractions(accountMapper, eventPublisher);
    }

    @Test
    void createAccount_singleCurrency_insertsAccountAndBalance_publishesEvent() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-001");
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR));

        AccountResponse response = accountService.createAccount(request);

        verify(accountMapper).insertAccount(any(Account.class));
        verify(accountMapper, times(1)).insertBalance(any(Balance.class));

        assertThat(response.getCustomerId()).isEqualTo("customer-001");
        assertThat(response.getBalances()).hasSize(1);
        assertThat(response.getBalances().getFirst().getMoney().getAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getBalances().getFirst().getMoney().getCurrencyCode()).isEqualTo(Currency.EUR);

        ArgumentCaptor<AccountCreatedEvent> eventCaptor = ArgumentCaptor.forClass(AccountCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().account().getCustomerId()).isEqualTo("customer-001");
    }

    @Test
    void createAccount_multipleCurrencies_insertsOneBalancePerCurrency() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId("customer-001");
        request.setCountry("EE");
        request.setCurrencies(List.of(Currency.EUR, Currency.USD, Currency.GBP));

        AccountResponse response = accountService.createAccount(request);

        verify(accountMapper, times(3)).insertBalance(any(Balance.class));
        assertThat(response.getBalances()).hasSize(3);
    }

    @Test
    void getAccount_accountDoesNotExist_throwsAccountNotFoundException() {
        UUID accountId = UUID.randomUUID();
        when(accountMapper.findAccountById(accountId)).thenReturn(null);

        assertThatThrownBy(() -> accountService.getAccount(accountId))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAccount_accountExists_returnsAccountWithBalances() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account();
        account.setId(accountId);
        account.setCustomerId("customer-001");
        account.setCountry("EE");

        Balance balance = new Balance();
        balance.setAccountId(accountId);
        balance.setCurrency(Currency.EUR);
        balance.setAmount(BigDecimal.TEN);

        when(accountMapper.findAccountById(accountId)).thenReturn(account);
        when(accountMapper.findBalancesByAccountId(accountId)).thenReturn(List.of(balance));

        AccountResponse response = accountService.getAccount(accountId);

        assertThat(response.getAccountId()).isEqualTo(accountId);
        assertThat(response.getBalances()).hasSize(1);
        assertThat(response.getBalances().getFirst().getMoney().getAmount()).isEqualByComparingTo(BigDecimal.TEN);
    }

}
