package com.hmetsallik.corebanking.transaction.event;

import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;

public record TransactionCreatedEvent(TransactionResponse transaction) {
}