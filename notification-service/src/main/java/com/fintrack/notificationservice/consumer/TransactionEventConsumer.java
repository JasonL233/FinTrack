package com.fintrack.notificationservice.consumer;

import com.fintrack.notificationservice.event.TransactionCreatedEvent;
import com.fintrack.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);
    
    private final EmailService emailService;

    public TransactionEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "transaction-created-events", groupId = "notification-service-group")
    public void consumeTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.info("✅ Received transaction event: eventId={}, transactionId={}, userId={}, amount={}", 
                event.getEventId(), event.getTransactionId(), event.getUserId(), event.getAmount());
        
        try {
            // Send email notification
            emailService.sendTransactionNotification(
                event.getUserEmail(),
                event.getType(),
                event.getAmount(),
                event.getCategory(),
                event.getMerchant()
            );
            
            log.info("✅ Successfully processed event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("❌ Failed to process event: {}", event.getEventId(), e);
        }
    }
}