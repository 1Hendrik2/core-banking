package com.hmetsallik.corebanking.transaction;

import com.hmetsallik.corebanking.account.AccountMapper;
import com.hmetsallik.corebanking.account.AccountNotFoundException;
import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.transaction.domain.Transaction;
import com.hmetsallik.corebanking.transaction.dto.CreateTransactionRequest;
import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;
import com.hmetsallik.corebanking.transaction.exception.CurrencyNotSupportedException;
import com.hmetsallik.corebanking.transaction.exception.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public TransactionResponse createTransaction(UUID accountId, CreateTransactionRequest request) {
        Account account = accountMapper.findAccountById(accountId);

        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        Balance balance = accountMapper.findBalanceForUpdate(accountId, request.getCurrency());
        if (balance == null) {
            throw new CurrencyNotSupportedException(accountId, request.getCurrency());
        }

        BigDecimal newAmount = request.getDirection() == Direction.IN
                ? balance.getAmount().add(request.getAmount())
                : balance.getAmount().subtract(request.getAmount());

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(accountId, request.getCurrency());
        }

        balance.setAmount(newAmount);
        balance.setUpdatedAt(Instant.now());
        accountMapper.updateBalance(balance);

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDirection(request.getDirection());
        transaction.setDescription(request.getDescription());
        transaction.setBalanceAfter(newAmount);
        transaction.setCreatedAt(Instant.now());
        transactionMapper.insertTransaction(transaction);

        return toResponse(transaction);
    }

    public List<TransactionResponse> getTransactions(UUID accountId) {
        Account account = accountMapper.findAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        return transactionMapper.findTransactionsByAccountId(accountId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getAccountId(),
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDirection(),
                transaction.getDescription(),
                transaction.getBalanceAfter()
        );
    }
}
