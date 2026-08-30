package com.hmetsallik.corebanking.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AccountResponse {
    private UUID accountId;
    private String customerId;
    private List<BalanceResponse> balances;
}
