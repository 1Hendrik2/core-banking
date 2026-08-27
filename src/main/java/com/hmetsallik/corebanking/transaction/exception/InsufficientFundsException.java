package com.hmetsallik.corebanking.transaction.exception;

import com.hmetsallik.corebanking.common.Currency;

import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(UUID accountId, Currency currency) {
        super("Insufficient funds on account " + accountId + " in " + currency);
    }
}
