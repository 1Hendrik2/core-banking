package com.hmetsallik.corebanking.messaging;

import com.hmetsallik.corebanking.account.event.AccountCreatedEvent;
import com.hmetsallik.corebanking.transaction.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountCreated(AccountCreatedEvent event) {
        publish(RabbitMQConfig.ACCOUNT_CREATED_ROUTING_KEY, event.account());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        publish(RabbitMQConfig.TRANSACTION_CREATED_ROUTING_KEY, event.transaction());
    }

    private void publish(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, payload);
        } catch (AmqpException ex) {
            log.error("Failed to publish event with routing key '{}': {}", routingKey, ex.getMessage(), ex);
        }
    }
}
