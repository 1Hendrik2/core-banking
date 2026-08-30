package com.hmetsallik.corebanking.transaction;

import com.hmetsallik.corebanking.transaction.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TransactionMapper {
    void insertTransaction(Transaction transaction);
    List<Transaction> findTransactionsByAccountId(UUID accountId);
}
