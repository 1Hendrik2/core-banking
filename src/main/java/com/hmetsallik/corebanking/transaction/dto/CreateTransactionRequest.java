package com.hmetsallik.corebanking.transaction.dto;

import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.common.dto.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "money is required")
    @Valid
    private Money money;

    @NotNull(message = "direction is required")
    private Direction direction;

    @NotBlank(message = "description is required")
    private String description;
}
