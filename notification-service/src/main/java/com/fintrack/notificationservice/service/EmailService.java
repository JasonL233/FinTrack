package com.fintrack.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendTransactionNotification(String email, String type, BigDecimal amount, 
                                           String category, String merchant) {
        // SIMPLE: Just log for now (sufficient for resume purposes)
        String message = String.format(
            "💰 Transaction Alert: %s of $%.2f for %s%s",
            type, amount, category, 
            merchant != null ? " at " + merchant : ""
        );
        
        log.info("📧 Sending email to {}: {}", email, message);
        
        // This demonstrates the pipeline works - can add real JavaMailSender later if needed
    }
}