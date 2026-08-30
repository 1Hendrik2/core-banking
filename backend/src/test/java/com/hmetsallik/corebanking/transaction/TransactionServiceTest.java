package com.hmetsallik.corebanking.transaction;

import com.hmetsallik.corebanking.account.AccountMapper;
import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.account.exception.AccountNotFoundException;
import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.common.dto.Money;
import com.hmetsallik.corebanking.transaction.domain.Transaction;
import com.hmetsallik.corebanking.transaction.dto.CreateTransactionRequest;
import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;
import com.hmetsallik.corebanking.transaction.event.TransactionCreatedEvent;
import com.hmetsallik.corebanking.transaction.exception.CurrencyNotSupportedException;
import com.hmetsallik.corebanking.transaction.exception.InsufficientFundsException;
import com.hmetsallik.corebanking.transaction.exception.InvalidAmountException;
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
class TransactionServiceTest {

    @Mock
    private AccountMapper accountMapper;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransactionService transactionService;

    private final UUID accountId = UUID.randomUUID();

    @Test
    void createTransaction_accountDoesNotExist_throwsAccountNotFoundException() {
        when(accountMapper.findAccountById(accountId)).thenReturn(null);

        CreateTransactionRequest request = requestOf(BigDecimal.TEN, Currency.EUR, Direction.IN);

        assertThatThrownBy(() -> transactionService.createTransaction(accountId, request))
                .isInstanceOf(AccountNotFoundException.class);

        verifyNoInteractions(transactionMapper, eventPublisher);
    }

    @Test
    void createTransaction_amountIsZero_throwsInvalidAmountException() {
        when(accountMapper.findAccountById(accountId)).thenReturn(new Account());

        CreateTransactionRequest request = requestOf(BigDecimal.ZERO, Currency.EUR, Direction.IN);

        assertThatThrownBy(() -> transactionService.createTransaction(accountId, request))
                .isInstanceOf(InvalidAmountException.class);

        verify(accountMapper, never()).findBalanceForUpdate(any(), any());
    }

    @Test
    void createTransaction_amountIsNegative_throwsInvalidAmountException() {
        when(accountMapper.findAccountById(accountId)).thenReturn(new Account());

        CreateTransactionRequest request = requestOf(BigDecimal.valueOf(-5), Currency.EUR, Direction.OUT);

        assertThatThrownBy(() -> transactionService.createTransaction(accountId, request))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void createTransaction_noBalanceInRequestedCurrency_throwsCurrencyNotSupportedException() {
        when(accountMapper.findAccountById(accountId)).thenReturn(new Account());
        when(accountMapper.findBalanceForUpdate(accountId, Currency.USD)).thenReturn(null);

        CreateTransactionRequest request = requestOf(BigDecimal.TEN, Currency.USD, Direction.IN);

        assertThatThrownBy(() -> transactionService.createTransaction(accountId, request))
                .isInstanceOf(CurrencyNotSupportedException.class);
    }

    @Test
    void createTransaction_withdrawalExceedsBalance_throwsInsufficientFundsException() {
        when(accountMapper.findAccountById(accountId)).thenReturn(new Account());
        Balance balance = balanceOf(BigDecimal.valueOf(50));
        when(accountMapper.findBalanceForUpdate(accountId, Currency.EUR)).thenReturn(balance);

        CreateTransactionRequest request = requestOf(BigDecimal.valueOf(100), Currency.EUR, Direction.OUT);

        assertThatThrownBy(() -> transactionService.createTransaction(accountId, request))
                .isInstanceOf(InsufficientFundsException.class);

        verify(accountMapper, never()).updateBalance(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createTransaction_deposit_updatesBalanceAndPublishesEvent() {
        when(accountMapper.findAccountById(accountId)).thenReturn(new Account());
        Balance balance = balanceOf(BigDecimal.valueOf(100));
        when(accountMapper.findBalanceForUpdate(accountId, Currency.EUR)).thenReturn(balance);

        CreateTransactionRequest request = requestOf(BigDecimal.valueOf(50), Currency.EUR, Direction.IN);

        TransactionResponse response = transactionService.createTransaction(accountId, request);

        assertThat(response.getBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(response.getDirection()).isEqualTo(Direction.IN);

        verify(accountMapper).updateBalance(argThat(b -> b.getAmount().compareTo(BigDecimal.valueOf(150)) == 0));
        verify(transactionMapper).insertTransaction(any(Transaction.class));

        ArgumentCaptor<TransactionCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().transaction().getBalanceAfter())
                .isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void createTransaction_withdrawal_updatesBalanceAndPublishesEvent() {
        when(accountMapper.findAccountById(accountId)).thenReturn(new Account());
        Balance balance = balanceOf(BigDecimal.valueOf(100));
        when(accountMapper.findBalanceForUpdate(accountId, Currency.EUR)).thenReturn(balance);

        CreateTransactionRequest request = requestOf(BigDecimal.valueOf(40), Currency.EUR, Direction.OUT);

        TransactionResponse response = transactionService.createTransaction(accountId, request);

        assertThat(response.getBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(60));
        verify(accountMapper).updateBalance(argThat(b -> b.getAmount().compareTo(BigDecimal.valueOf(60)) == 0));
    }

    @Test
    void getTransactions_accountDoesNotExist_throwsAccountNotFoundException() {
        when(accountMapper.findAccountById(accountId)).thenReturn(null);

        assertThatThrownBy(() -> transactionService.getTransactions(accountId))
                .isInstanceOf(AccountNotFoundException.class);

        verifyNoInteractions(transactionMapper);
    }

    @Test
    void getTransactions_accountExists_returnsMappedResponses() {
        when(accountMapper.findAccountById(accountId)).thenReturn(new Account());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setCurrency(Currency.EUR);
        transaction.setDirection(Direction.IN);
        transaction.setDescription("test");
        transaction.setBalanceAfter(BigDecimal.TEN);

        when(transactionMapper.findTransactionsByAccountId(accountId)).thenReturn(List.of(transaction));

        List<TransactionResponse> responses = transactionService.getTransactions(accountId);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getTransactionId()).isEqualTo(transaction.getId());
    }

    private CreateTransactionRequest requestOf(BigDecimal amount, Currency currency, Direction direction) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setMoney(new Money(amount, currency));
        request.setDirection(direction);
        request.setDescription("test transaction");
        return request;
    }

    private Balance balanceOf(BigDecimal amount) {
        Balance balance = new Balance();
        balance.setAccountId(accountId);
        balance.setCurrency(Currency.EUR);
        balance.setAmount(amount);
        return balance;
    }
}
