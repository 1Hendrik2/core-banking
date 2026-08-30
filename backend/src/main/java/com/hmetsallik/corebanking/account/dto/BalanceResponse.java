package com.hmetsallik.corebanking.account.dto;

import com.hmetsallik.corebanking.common.dto.Money;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceResponse {
    private Money money;
}
