package com.hmetsallik.corebanking.account.event;

import com.hmetsallik.corebanking.account.dto.AccountResponse;

public record AccountCreatedEvent(AccountResponse account) {
}
