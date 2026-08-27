package com.hmetsallik.corebanking.account.dto;

import com.hmetsallik.corebanking.common.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceResponse {
    private BigDecimal amount;
    private Currency currency;
}
