package com.hmetsallik.corebanking.account.domain;

import com.hmetsallik.corebanking.common.Currency;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class Balance {
    private UUID id;
    private UUID accountId;
    private Currency currency;
    private BigDecimal amount;
    private Instant updatedAt;
}
