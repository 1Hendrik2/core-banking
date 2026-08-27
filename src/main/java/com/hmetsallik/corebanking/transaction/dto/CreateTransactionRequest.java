package com.hmetsallik.corebanking.transaction.dto;

import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "currency is required")
    private Currency currency;

    @NotNull(message = "direction is required")
    private Direction direction;

    @NotBlank(message = "description is required")
    private String description;
}
