package com.hmetsallik.corebanking.account.dto;

import com.hmetsallik.corebanking.common.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "country is required")
    private String customerId;

    @NotBlank(message = "country is required")
    private String country;

    @NotEmpty(message = "at least one currency is required")
    private List<Currency> currencies;
}
