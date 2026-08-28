package com.hmetsallik.corebanking.messaging;

import com.hmetsallik.corebanking.account.dto.AccountResponse;
import com.hmetsallik.corebanking.account.event.AccountCreatedEvent;
import com.hmetsallik.corebanking.transaction.dto.TransactionResponse;
import com.hmetsallik.corebanking.transaction.event.TransactionCreatedEvent;
import com.hmetsallik.corebanking.common.Currency;
import com.hmetsallik.corebanking.common.Direction;
import com.hmetsallik.corebanking.common.dto.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class RabbitMQEventListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQEventListener listener;

    @Test
    void onAccountCreated_publishesToCorrectExchangeAndRoutingKey() {
        AccountResponse account = new AccountResponse(UUID.randomUUID(), "cust-1", List.of());

        listener.onAccountCreated(new AccountCreatedEvent(account));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.ACCOUNT_CREATED_ROUTING_KEY), eq(account));
    }

    @Test
    void onTransactionCreated_publishesToCorrectExchangeAndRoutingKey() {
        TransactionResponse transaction = new TransactionResponse(
                UUID.randomUUID(), UUID.randomUUID(), new Money(BigDecimal.TEN, Currency.EUR),
                Direction.IN, "test", BigDecimal.TEN);

        listener.onTransactionCreated(new TransactionCreatedEvent(transaction));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.TRANSACTION_CREATED_ROUTING_KEY), eq(transaction));
    }

    @Test
    void onAccountCreated_brokerThrowsAmqpException_doesNotPropagate() {
        AccountResponse account = new AccountResponse(UUID.randomUUID(), "cust-1", List.of());

        doThrow(new AmqpException("broker unreachable"))
                .when(rabbitTemplate).convertAndSend(any(), any(), any(Object.class));

        assertThatCode(() -> listener.onAccountCreated(new AccountCreatedEvent(account)))
                .doesNotThrowAnyException();
    }

    @Test
    void onTransactionCreated_brokerThrowsAmqpException_doesNotPropagate() {
        TransactionResponse transaction = new TransactionResponse(
                UUID.randomUUID(), UUID.randomUUID(), new Money(BigDecimal.TEN, Currency.EUR),
                Direction.IN, "test", BigDecimal.TEN);

        doThrow(new AmqpException("broker unreachable"))
                .when(rabbitTemplate).convertAndSend(any(), any(), any(Object.class));

        assertThatCode(() -> listener.onTransactionCreated(new TransactionCreatedEvent(transaction)))
                .doesNotThrowAnyException();
    }
}
