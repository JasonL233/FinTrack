package com.fintrack.transactionservice.service;

import com.fintrack.transactionservice.event.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    @Value("${kafka.topic.transaction-created}")
    private String transactionCreatedTopic;

    public KafkaProducerService(KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.info("Publishing transaction created event: eventId={}, transactionId={}, userId={}, amount={}",
                event.getEventId(), event.getTransactionId(), event.getUserId(), event.getAmount());
        
        try {
            // Send to Kafka (async)
            CompletableFuture<SendResult<String, TransactionCreatedEvent>> future = 
                    kafkaTemplate.send(
                        transactionCreatedTopic, 
                        event.getTransactionId().toString(), // Key
                        event  // Value
                    );

            // Add callback for success/failure
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published event: eventId={} to topic={} partition={} offset={}",
                            event.getEventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event: eventId={}, error={}",
                            event.getEventId(), ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Exception while publishing event: eventId={}, error={}",
                    event.getEventId(), e.getMessage(), e);
        }
    }
}