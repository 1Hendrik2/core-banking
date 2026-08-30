package com.hmetsallik.corebanking.transaction.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(BigDecimal amount) {
        super("Amount must be greater than zero: " + amount);
    }
}
