package com.hmetsallik.corebanking.transaction;

import com.hmetsallik.corebanking.account.AccountMapper;
import com.hmetsallik.corebanking.account.exception.AccountNotFoundException;
import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.transaction.domain.Transaction;
import com.hmetsallik.corebanking.transaction.dto.CreateTransactionRequest;
import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;
import com.hmetsallik.corebanking.transaction.event.TransactionCreatedEvent;
import com.hmetsallik.corebanking.transaction.exception.CurrencyNotSupportedException;
import com.hmetsallik.corebanking.transaction.exception.InsufficientFundsException;
import com.hmetsallik.corebanking.transaction.exception.InvalidAmountException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionResponse createTransaction(UUID accountId, CreateTransactionRequest request) {
        Account account = accountMapper.findAccountById(accountId);

        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        BigDecimal requestedAmount = request.getMoney().getAmount();
        Currency currency = request.getMoney().getCurrencyCode();

        if (requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(requestedAmount);
        }

        Balance balance = accountMapper.findBalanceForUpdate(accountId, currency);
        if (balance == null) {
            throw new CurrencyNotSupportedException(accountId, currency);
        }

        BigDecimal newAmount = request.getDirection() == Direction.IN
                ? balance.getAmount().add(requestedAmount)
                : balance.getAmount().subtract(requestedAmount);

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(accountId, currency);
        }

        balance.setAmount(newAmount);
        balance.setUpdatedAt(Instant.now());
        accountMapper.updateBalance(balance);

        Transaction transaction = TransactionAssembler.toDomain(
                accountId, requestedAmount, currency, request.getDirection(), request.getDescription(), newAmount);
        transactionMapper.insertTransaction(transaction);

        TransactionResponse response = TransactionAssembler.toResponse(transaction);
        eventPublisher.publishEvent(new TransactionCreatedEvent(response));
        return response;
    }

    public List<TransactionResponse> getTransactions(UUID accountId) {
        Account account = accountMapper.findAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        return transactionMapper.findTransactionsByAccountId(accountId).stream()
                .map(TransactionAssembler::toResponse)
                .collect(Collectors.toList());
    }
}
