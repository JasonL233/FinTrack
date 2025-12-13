# 📋 Notification Service - Event Consumer Summary

## 🎯 Overview

The Notification Service is an **event-driven microservice** that consumes transaction events from Kafka and sends notifications to users. It demonstrates the **consumer side** of the event-driven architecture, reacting to transaction creation events in real-time.

**Key Characteristics:**

- **Stateless Service** - No database required, purely event-driven
- **Kafka Consumer** - Subscribes to `transaction-created-events` topic
- **Async Processing** - Processes events asynchronously as they arrive
- **Notification Sender** - Sends email notifications (currently logs for demo)

---

## 🏗️ Architecture

### Service Architecture

```
┌─────────────────────────────────────┐
│    Kafka Broker                     │
│    Topic: transaction-created-events│
└──────────────┬──────────────────────┘
               │
               │ Consumes Events
               ↓
┌─────────────────────────────────────┐
│    Notification Service             │
│    (Port 8083)                      │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ TransactionEventConsumer     │  │
│  │ - Listens to Kafka events    │  │
│  │ - Processes each event       │  │
│  └──────────────┬───────────────┘  │
│                 │                   │
│  ┌──────────────▼───────────────┐  │
│  │ EmailService                 │  │
│  │ - Formats notification       │  │
│  │ - Sends email (logs for now) │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

### Component Layers

```
┌─────────────────────────────────────┐
│    Consumer Layer                   │
│    (TransactionEventConsumer)       │
│    - Kafka listener                 │
│    - Event handler                  │
│    - Error handling                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    Service Layer                    │
│    (EmailService)                   │
│    - Notification formatting        │
│    - Email sending logic            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    Configuration Layer              │
│    (KafkaConsumerConfig)            │
│    - Kafka consumer factory         │
│    - Deserialization setup          │
└─────────────────────────────────────┘
```

---

## 📨 Event Consumption

### Kafka Topic

**Topic:** `transaction-created-events`

**Consumer Group:** `notification-service-group`

### Event Structure

The service consumes `TransactionCreatedEvent` with the following structure:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventTimestamp": "2025-12-13T01:31:09.897963",
  "transactionId": 7,
  "userId": 7,
  "userEmail": "user-7@fintrack.com",
  "amount": 50.0,
  "type": "EXPENSE",
  "category": "GROCERIES",
  "description": "Test notification",
  "transactionDate": "2024-12-13",
  "merchant": null,
  "referenceNumber": "TXN-FDB0E56E",
  "createdAt": "2025-12-13T01:31:09.897963"
}
```

### Event Fields Used

| Field           | Used For                          |
| --------------- | --------------------------------- |
| `userEmail`     | Recipient email address           |
| `type`          | Transaction type (INCOME/EXPENSE) |
| `amount`        | Transaction amount                |
| `category`      | Transaction category              |
| `merchant`      | Merchant name (optional)          |
| `eventId`       | Event tracking and logging        |
| `transactionId` | Transaction reference             |

---

## 🔄 Event Processing Flow

### Complete Flow Diagram

```
Transaction Service
  │
  ├─ Creates transaction
  │
  ├─ Publishes event to Kafka
  │     ↓
  │
  └─ Kafka Topic: transaction-created-events
         │
         │ Event Available
         ↓
Notification Service
  │
  ├─ TransactionEventConsumer.consumeTransactionCreatedEvent()
  │     ├─ Receive event from Kafka
  │     ├─ Log event receipt
  │     └─ Extract event data
  │           ↓
  ├─ EmailService.sendTransactionNotification()
  │     ├─ Format notification message
  │     ├─ Include transaction details
  │     └─ Send email (currently logs)
  │           ↓
  └─ Log success/failure
```

### Detailed Processing Steps

**1. Event Reception:**

```java
@KafkaListener(topics = "transaction-created-events", groupId = "notification-service-group")
public void consumeTransactionCreatedEvent(TransactionCreatedEvent event) {
    log.info("✅ Received transaction event: eventId={}, transactionId={}, userId={}, amount={}",
            event.getEventId(), event.getTransactionId(), event.getUserId(), event.getAmount());
}
```

**2. Notification Creation:**

```java
emailService.sendTransactionNotification(
    event.getUserEmail(),    // To
    event.getType(),         // Transaction type
    event.getAmount(),       // Amount
    event.getCategory(),     // Category
    event.getMerchant()      // Merchant (optional)
);
```

**3. Message Formatting:**

```java
String message = String.format(
    "💰 Transaction Alert: %s of $%.2f for %s%s",
    type,                    // EXPENSE or INCOME
    amount,                  // 50.00
    category,                // GROCERIES
    merchant != null ? " at " + merchant : ""
);
// Result: "💰 Transaction Alert: EXPENSE of $50.00 for GROCERIES"
```

**4. Notification Delivery:**

```java
log.info("📧 Sending email to {}: {}", email, message);
// Currently logs the message (can be extended to send real emails)
```

---

## 🏗️ Components

### 1. TransactionEventConsumer

**Location:** `com.fintrack.notificationservice.consumer.TransactionEventConsumer`

**Responsibilities:**

- Listens to Kafka topic `transaction-created-events`
- Processes incoming transaction events
- Handles errors gracefully
- Logs event processing status

**Key Features:**

- `@KafkaListener` annotation for automatic event consumption
- Error handling with try-catch
- Comprehensive logging

**Example Log Output:**

```
INFO  - ✅ Received transaction event: eventId=b069024f..., transactionId=7, userId=7, amount=50.00
INFO  - 📧 Sending email to user-7@fintrack.com: 💰 Transaction Alert: EXPENSE of $50.00 for GROCERIES
INFO  - ✅ Successfully processed event: b069024f...
```

### 2. EmailService

**Location:** `com.fintrack.notificationservice.service.EmailService`

**Responsibilities:**

- Formats notification messages
- Handles email sending logic
- Currently logs notifications (can be extended to send real emails)

**Current Implementation:**

- Logs notification messages
- Ready to integrate with JavaMailSender for real email sending

**Future Enhancement:**

- Integrate with SMTP server
- Support HTML email templates
- Add email queue for reliability

### 3. KafkaConsumerConfig

**Location:** `com.fintrack.notificationservice.config.KafkaConsumerConfig`

**Configuration:**

- Consumer factory setup
- JSON deserialization for events
- Consumer group configuration
- Listener container factory

**Key Settings:**

- `GROUP_ID_CONFIG`: `notification-service-group`
- `KEY_DESERIALIZER`: `StringDeserializer` (transaction ID as key)
- `VALUE_DESERIALIZER`: `JsonDeserializer` (event object)
- `TRUSTED_PACKAGES`: `*` (allows deserialization from any package)

### 4. TransactionCreatedEvent

**Location:** `com.fintrack.notificationservice.event.TransactionCreatedEvent`

**Purpose:**

- Event data structure
- Matches producer's event structure
- Simple POJO with getters/setters

**Note:** This is a local copy of the event class. In production, consider:

- Shared event library (common module)
- Schema registry for versioning
- Protobuf/Avro for schema evolution

---

## 🔧 Configuration

### Application Configuration

**application.yml:**

```yaml
server:
  port: 8083

spring:
  application:
    name: notification-service

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: notification-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
```

### Key Configuration Properties

| Property                         | Value                        | Description                               |
| -------------------------------- | ---------------------------- | ----------------------------------------- |
| `server.port`                    | `8083`                       | HTTP server port                          |
| `spring.kafka.bootstrap-servers` | `localhost:9092`             | Kafka broker addresses                    |
| `spring.kafka.consumer.group-id` | `notification-service-group` | Consumer group ID                         |
| `auto-offset-reset`              | `earliest`                   | Start reading from beginning if no offset |
| `key-deserializer`               | `StringDeserializer`         | Deserialize message keys as strings       |
| `value-deserializer`             | `JsonDeserializer`           | Deserialize message values as JSON        |

### Environment Variables

| Variable                  | Default          | Description            |
| ------------------------- | ---------------- | ---------------------- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker addresses |

---

## 🔄 Consumer Behavior

### Offset Management

**Auto Offset Reset:** `earliest`

- If consumer group has no committed offset, starts from the beginning
- Ensures no events are missed on first run
- After processing, offsets are committed automatically

**Offset Committing:**

- Automatic commit after successful message processing
- Managed by Spring Kafka framework
- Commit happens after listener method completes successfully

### Consumer Group

**Group ID:** `notification-service-group`

**Benefits:**

- Multiple instances can share the load (load balancing)
- Each instance processes a subset of partitions
- If one instance fails, others continue processing
- Offset tracking per group (independent of other consumers)

### Error Handling

**Current Strategy:**

- Try-catch around processing logic
- Errors are logged but don't stop consumer
- Failed events are skipped (no retry mechanism yet)

**Future Enhancements:**

- Dead Letter Queue (DLQ) for failed events
- Retry mechanism with exponential backoff
- Error notification system
- Event replay capability

---

## 📊 Logging

### Log Levels

```yaml
logging:
  level:
    com.fintrack: DEBUG
    org.springframework.kafka: INFO
```

### Log Messages

**Event Reception:**

```
INFO  - ✅ Received transaction event: eventId=..., transactionId=7, userId=7, amount=50.00
```

**Notification Sent:**

```
INFO  - 📧 Sending email to user-7@fintrack.com: 💰 Transaction Alert: EXPENSE of $50.00 for GROCERIES
```

**Success:**

```
INFO  - ✅ Successfully processed event: b069024f-54f6-4aec-88e0-cc73552610f4
```

**Error:**

```
ERROR - ❌ Failed to process event: b069024f-54f6-4aec-88e0-cc73552610f4
```

**Consumer Lifecycle:**

```
INFO  - Subscribed to topic(s): transaction-created-events
INFO  - Successfully joined group with generation Generation{generationId=1, ...}
INFO  - partitions assigned: [transaction-created-events-0]
```

---

## 🎯 Key Features

✅ **Event-Driven Architecture** - Reacts to events as they happen  
✅ **Kafka Consumer** - Reliable event consumption with offset management  
✅ **Consumer Group** - Support for horizontal scaling  
✅ **JSON Deserialization** - Automatic conversion from JSON to Java objects  
✅ **Error Handling** - Graceful error handling with logging  
✅ **Comprehensive Logging** - Detailed logs for monitoring and debugging  
✅ **Stateless Service** - No database required, purely reactive  
✅ **Async Processing** - Non-blocking event processing  
✅ **Email Ready** - Structure in place for email integration  
✅ **Spring Kafka Integration** - Uses Spring Kafka abstractions

---

## 🔄 Integration with Other Services

### Transaction Service (Producer)

```
Transaction Service (Producer)
  │
  ├─ Creates transaction
  ├─ Publishes event to Kafka
  └─ Topic: transaction-created-events
         │
         │ (Async, decoupled)
         ↓
Notification Service (Consumer)
  │
  ├─ Consumes event
  ├─ Sends notification
  └─ Logs result
```

**Benefits:**

- **Loose Coupling** - Services don't directly call each other
- **Scalability** - Can add more consumers without changing producer
- **Resilience** - If notification service is down, transactions still work
- **Async** - Transaction creation doesn't wait for notification

---

## 🚀 Testing

### Manual Testing

**1. Start Services:**

```bash
# Start Kafka
docker-compose up -d zookeeper kafka

# Start notification service
./start-services.sh notification-service
```

**2. Watch Logs:**

```bash
tail -f logs/notification-service.log
```

**3. Create Transaction:**

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jlin18@gmail.com","password":"password123"}' | jq -r '.data.token')

curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 50.00,
    "type": "EXPENSE",
    "category": "GROCERIES",
    "description": "Test notification",
    "transactionDate": "2024-12-13"
  }'
```

**4. Verify in Logs:**

- Should see event reception log
- Should see email notification log
- Should see success log

### Verify in Kafka UI

**Access:** http://localhost:8090

**Steps:**

1. Go to **Topics** → `transaction-created-events`
2. Check **Messages** tab to see published events
3. Go to **Consumer Groups** → `notification-service-group`
4. Verify **Lag** is 0 (all messages consumed)

---

## 🔍 Monitoring

### Consumer Lag

**Check consumer lag:**

```bash
docker exec -it fintrack-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group notification-service-group
```

**Expected Output:**

```
GROUP                      TOPIC                    PARTITION  CURRENT-OFFSET  LAG
notification-service-group transaction-created-events 0          5               0
```

- **CURRENT-OFFSET:** Last processed message offset
- **LAG:** Number of unprocessed messages (should be 0 if healthy)

### Health Indicators

**Healthy Service:**

- ✅ Logs show "Successfully joined group"
- ✅ Logs show "partitions assigned"
- ✅ Consumer lag is 0 or low
- ✅ Events are being processed

**Unhealthy Service:**

- ❌ No "Successfully joined group" log
- ❌ Consumer lag increasing
- ❌ Error logs appearing frequently
- ❌ No event processing logs

---

## 📝 Dependencies

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- Spring Mail (for future email integration) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
</dependencies>
```

---

## 🚀 Next Steps

### Short Term

- [ ] Implement real email sending with JavaMailSender
- [ ] Add HTML email templates
- [ ] Add email configuration (SMTP settings)
- [ ] Add notification preferences per user

### Medium Term

- [ ] Add retry mechanism for failed notifications
- [ ] Implement Dead Letter Queue (DLQ) for failed events
- [ ] Add notification types (SMS, push, etc.)
- [ ] Add notification history/tracking
- [ ] Add rate limiting for notifications

### Long Term

- [ ] Multi-channel notifications (email, SMS, push, in-app)
- [ ] Notification templates and customization
- [ ] Notification preferences API
- [ ] Notification analytics and reporting
- [ ] Webhook support for external integrations
- [ ] Scheduled notifications (digest emails)

---

## 🔐 Security Considerations

### Current Setup

- **No Authentication** - Service doesn't expose HTTP endpoints
- **Kafka Security** - Uses PLAINTEXT (dev only)
- **Event Data** - Contains user email addresses

### Production Recommendations

- [ ] Enable Kafka SASL/SSL authentication
- [ ] Encrypt sensitive data in events
- [ ] Add rate limiting for notifications
- [ ] Implement notification preferences
- [ ] Add audit logging for sent notifications
- [ ] Secure email sending (TLS for SMTP)

---

## 📋 Summary

The Notification Service is a **lightweight, event-driven microservice** that:

1. **Listens** to `transaction-created-events` from Kafka
2. **Processes** each event asynchronously
3. **Sends** notifications to users (currently logs)
4. **Logs** all processing activities for monitoring

**Key Benefits:**

- ✅ Decoupled from transaction service
- ✅ Scalable (can run multiple instances)
- ✅ Resilient (failures don't affect transaction creation)
- ✅ Simple and focused (single responsibility)

**Current State:**

- Event consumption: ✅ Working
- Notification logging: ✅ Working
- Email sending: ⏳ Ready for implementation

This service demonstrates the **consumer pattern** in event-driven architecture, showing how services can react to business events without tight coupling.
