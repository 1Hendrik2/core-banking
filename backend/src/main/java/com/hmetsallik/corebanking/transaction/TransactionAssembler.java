package com.hmetsallik.corebanking.transaction;

import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.common.dto.Money;
import com.hmetsallik.corebanking.transaction.domain.Transaction;
import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TransactionAssembler {
    private TransactionAssembler() {
    }

    public static Transaction toDomain(UUID accountId, BigDecimal amount, Currency currency,
                                       Direction direction, String description, BigDecimal balanceAfter) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDirection(direction);
        transaction.setDescription(description);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setCreatedAt(Instant.now());
        return transaction;
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getAccountId(),
                transaction.getId(),
                new Money(transaction.getAmount(), transaction.getCurrency()),
                transaction.getDirection(),
                transaction.getDescription(),
                transaction.getBalanceAfter()
        );
    }
}
