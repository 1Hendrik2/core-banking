package com.hmetsallik.corebanking.account.event;

import com.hmetsallik.corebanking.account.dto.AccountResponse;

public class AccountCreatedEvent {
    private final AccountResponse account;

    public AccountCreatedEvent(AccountResponse account) {
        this.account = account;
    }

    public AccountResponse getAccount() {
        return account;
    }
}
