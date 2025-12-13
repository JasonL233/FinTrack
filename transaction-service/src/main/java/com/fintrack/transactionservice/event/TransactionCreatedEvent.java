package com.fintrack.transactionservice.event;

import com.fintrack.transactionservice.entity.TransactionCategory;
import com.fintrack.transactionservice.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionCreatedEvent {
    private Long transactionId;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionCategory category;
    private String description;
    private LocalDate transactionDate;
    private String merchant;
    private String referenceNumber;
    private LocalDateTime createdAt;
    private String eventId;
    private LocalDateTime eventTimestamp;

    public TransactionCreatedEvent() {
    }

    public TransactionCreatedEvent(Long transactionId, Long userId, String userEmail,
                                  BigDecimal amount, TransactionType type, TransactionCategory category,
                                  String description, LocalDate transactionDate, String merchant,
                                  String referenceNumber, LocalDateTime createdAt,
                                  String eventId, LocalDateTime eventTimestamp) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.transactionDate = transactionDate;
        this.merchant = merchant;
        this.referenceNumber = referenceNumber;
        this.createdAt = createdAt;
        this.eventId = eventId;
        this.eventTimestamp = eventTimestamp;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long transactionId;
        private Long userId;
        private String userEmail;
        private BigDecimal amount;
        private TransactionType type;
        private TransactionCategory category;
        private String description;
        private LocalDate transactionDate;
        private String merchant;
        private String referenceNumber;
        private LocalDateTime createdAt;
        private String eventId;
        private LocalDateTime eventTimestamp;

        public Builder transactionId(Long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder category(TransactionCategory category) {
            this.category = category;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder transactionDate(LocalDate transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }

        public Builder merchant(String merchant) {
            this.merchant = merchant;
            return this;
        }

        public Builder referenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventTimestamp(LocalDateTime eventTimestamp) {
            this.eventTimestamp = eventTimestamp;
            return this;
        }

        public TransactionCreatedEvent build() {
            return new TransactionCreatedEvent(transactionId, userId, userEmail, amount, type,
                    category, description, transactionDate, merchant, referenceNumber,
                    createdAt, eventId, eventTimestamp);
        }
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    @Override
    public String toString() {
        return "TransactionCreatedEvent{" +
                "eventId='" + eventId + '\'' +
                ", transactionId=" + transactionId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", type=" + type +
                ", category=" + category +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }
}