package com.hmetsallik.corebanking.account.domain;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class Account {
    private UUID id;
    private String customerId;
    private String country;
    private Instant createdAt;
}
