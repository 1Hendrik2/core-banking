package com.hmetsallik.corebanking.account;

import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.dto.BalanceResponse;
import com.hmetsallik.corebanking.account.dto.CreateAccountRequest;
import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.dto.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AccountAssembler {

    private AccountAssembler() {
    }

    public static Account toDomain(CreateAccountRequest request) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setCustomerId(request.getCustomerId());
        account.setCountry(request.getCountry());
        account.setCreatedAt(Instant.now());
        return account;
    }

    public static Balance toBalance(UUID accountId, Currency currency) {
        Balance balance = new Balance();
        balance.setId(UUID.randomUUID());
        balance.setAccountId(accountId);
        balance.setCurrency(currency);
        balance.setAmount(BigDecimal.ZERO);
        balance.setUpdatedAt(Instant.now());
        return balance;
    }

    public static BalanceResponse toResponse(Balance balance) {
        return new BalanceResponse(new Money(balance.getAmount(), balance.getCurrency()));
    }

    public static AccountResponse toResponse(Account account, List<BalanceResponse> balances) {
        return new AccountResponse(account.getId(), account.getCustomerId(), balances);
    }
}