package com.hmetsallik.corebanking.account;

import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.dto.BalanceResponse;
import com.hmetsallik.corebanking.account.dto.CreateAccountRequest;
import com.hmetsallik.corebanking.account.event.AccountCreatedEvent;
import com.hmetsallik.corebanking.account.exception.AccountNotFoundException;
import com.hmetsallik.corebanking.account.exception.DuplicateCurrencyException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountMapper accountMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (new HashSet<>(request.getCurrencies()).size() != request.getCurrencies().size()) {
            throw new DuplicateCurrencyException(request.getCurrencies());
        }
        Account account = AccountAssembler.toDomain(request);
        accountMapper.insertAccount(account);

        List<BalanceResponse> balances = request.getCurrencies().stream()
                .map(currency -> {
                    Balance balance = AccountAssembler.toBalance(account.getId(), currency);
                    accountMapper.insertBalance(balance);
                    return AccountAssembler.toResponse(balance);
                })
                .collect(Collectors.toList());

        AccountResponse response = AccountAssembler.toResponse(account, balances);
        eventPublisher.publishEvent(new AccountCreatedEvent(response));
        return response;
    }

    public AccountResponse getAccount(UUID accountId) {
        Account account = accountMapper.findAccountById(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }

        List<BalanceResponse> balances = accountMapper.findBalancesByAccountId(accountId).stream()
                .map(AccountAssembler::toResponse)
                .collect(Collectors.toList());

        return AccountAssembler.toResponse(account, balances);
    }
}
