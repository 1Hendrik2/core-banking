package com.hmetsallik.corebanking.account;

import com.hmetsallik.corebanking.account.domain.Account;
import com.hmetsallik.corebanking.account.domain.Balance;
import com.hmetsallik.corebanking.common.Currency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface AccountMapper {
    void insertAccount(Account account);
    void insertBalance(Balance balance);
    Account findAccountById(UUID id);
    List<Balance> findBalancesByAccountId(UUID accountId);

    Balance findBalanceForUpdate(@Param("accountId") UUID accountId, @Param("currency") Currency currency);
    void updateBalance(Balance balance);
}
