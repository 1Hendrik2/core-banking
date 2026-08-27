package com.hmetsallik.corebanking.transaction.event;

import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;

public class TransactionCreatedEvent {
    private final TransactionResponse transaction;

    public TransactionCreatedEvent(TransactionResponse transaction) {
        this.transaction = transaction;
    }

    public TransactionResponse getTransaction() {
        return transaction;
    }
}
