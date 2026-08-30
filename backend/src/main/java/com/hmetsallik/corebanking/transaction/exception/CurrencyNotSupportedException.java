package com.hmetsallik.corebanking.transaction.exception;

import com.hmetsallik.corebanking.common.Currency;

import java.util.UUID;

public class CurrencyNotSupportedException extends RuntimeException  {
    public CurrencyNotSupportedException(UUID accountId, Currency currency) {
        super("Account " + accountId + " does not hold a balance in " + currency);
    }
}
