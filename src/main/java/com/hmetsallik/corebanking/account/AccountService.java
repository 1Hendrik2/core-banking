package com.hmetsallik.corebanking.account;

import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.dto.BalanceResponse;
import com.hmetsallik.corebanking.account.dto.CreateAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountMapper accountMapper;

    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setCustomerId(request.getCustomerId());
        account.setCountry(request.getCountry());
        account.setCreatedAt(Instant.now());
        accountMapper.insertAccount(account);

        List<BalanceResponse> balances = request.getCurrencies().stream()
                .map(currency -> {
                    Balance balance = new Balance();
                    balance.setId(UUID.randomUUID());
                    balance.setAccountId(account.getId());
                    balance.setCurrency(currency);
                    balance.setAmount(BigDecimal.ZERO);
                    balance.setUpdatedAt(Instant.now());
                    accountMapper.insertBalance(balance);
                    return new BalanceResponse(balance.getAmount(), balance.getCurrency());
                })
                .collect(Collectors.toList());

        return new AccountResponse(account.getId(), account.getCustomerId(), balances);
    }

    public AccountResponse getAccount(UUID accountId) {
        Account account = accountMapper.findAccountById(accountId);
        if (account == null) {
           throw new AccountNotFoundException(accountId);
        }

        List<BalanceResponse> balances = accountMapper.findBalancesByAccountId(accountId).stream()
                .map(balance -> new BalanceResponse(balance.getAmount(), balance.getCurrency()))
                .collect(Collectors.toList());

        return new AccountResponse(account.getId(), account.getCustomerId(), balances);
    }
}
