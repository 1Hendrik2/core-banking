package com.hmetsallik.corebanking.common.dto;

import com.hmetsallik.corebanking.common.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Money {

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    @NotNull(message = "currencyCode is required")
    private Currency currencyCode;
}
