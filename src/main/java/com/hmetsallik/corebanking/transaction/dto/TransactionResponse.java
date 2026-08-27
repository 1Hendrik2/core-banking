package com.hmetsallik.corebanking.transaction.dto;

import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.common.dto.Money;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private UUID accountId;
    private UUID transactionId;
    private Money money;
    private Direction direction;
    private String description;
    private BigDecimal balanceAfter;
}
