# 📋 Kafka Implementation - Event-Driven Architecture Summary

## 🏗️ Infrastructure Overview

### Kafka Cluster Setup

**Components:**

- **Zookeeper** - Manages Kafka broker metadata and coordination
- **Kafka Broker** - Message broker for event streaming
- **Kafka UI** (Optional) - Web interface for monitoring and debugging

**Docker Compose Services:**

| Service   | Container Name       | Ports       | Description                   |
| --------- | -------------------- | ----------- | ----------------------------- |
| Zookeeper | `fintrack-zookeeper` | 2181        | Kafka metadata coordinator    |
| Kafka     | `fintrack-kafka`     | 9092, 29092 | Message broker                |
| Kafka UI  | `fintrack-kafka-ui`  | 8090        | Web-based Kafka management UI |

**Connection Details:**

- **Bootstrap Servers:** `localhost:9092` (external), `kafka:29092` (internal)
- **Zookeeper:** `localhost:2181`
- **Kafka UI:** `http://localhost:8090`

---

## 📨 Event Schema

### TransactionCreatedEvent

**Topic:** `transaction-created-events`

**Event Structure:**

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventTimestamp": "2025-12-13T01:10:30.123456",
  "transactionId": 1,
  "userId": 7,
  "userEmail": "jlin18@gmail.com",
  "amount": 125.5,
  "type": "EXPENSE",
  "category": "GROCERIES",
  "description": "Weekly shopping with Kafka",
  "transactionDate": "2024-12-10",
  "merchant": "Whole Foods",
  "referenceNumber": "TXN-D3D6D847",
  "createdAt": "2025-12-13T01:10:30.123456"
}
```

**Event Fields:**

| Field             | Type                | Description                                    |
| ----------------- | ------------------- | ---------------------------------------------- |
| `eventId`         | String (UUID)       | Unique identifier for the event                |
| `eventTimestamp`  | LocalDateTime       | When the event was created                     |
| `transactionId`   | Long                | ID of the created transaction                  |
| `userId`          | Long                | ID of the user who created the transaction     |
| `userEmail`       | String              | Email of the user                              |
| `amount`          | BigDecimal          | Transaction amount                             |
| `type`            | TransactionType     | INCOME or EXPENSE                              |
| `category`        | TransactionCategory | Transaction category (e.g., GROCERIES, SALARY) |
| `description`     | String              | Transaction description                        |
| `transactionDate` | LocalDate           | Date when transaction occurred                 |
| `merchant`        | String              | Merchant/vendor name                           |
| `referenceNumber` | String              | Unique transaction reference number            |
| `createdAt`       | LocalDateTime       | When transaction was created in database       |

**Message Key:**

- **Key Type:** String
- **Key Value:** `transactionId.toString()` (e.g., "1")
- **Purpose:** Ensures events for the same transaction are ordered and go to the same partition

**Message Value:**

- **Value Type:** `TransactionCreatedEvent` (JSON serialized)
- **Serialization:** JSON using Spring Kafka's `JsonSerializer`

---

## 🔧 Producer Configuration

### KafkaProducerConfig

**Location:** `com.fintrack.transactionservice.config.KafkaProducerConfig`

**Key Configuration:**

| Property                        | Value              | Description                           |
| ------------------------------- | ------------------ | ------------------------------------- |
| `BOOTSTRAP_SERVERS_CONFIG`      | `localhost:9092`   | Kafka broker addresses                |
| `KEY_SERIALIZER_CLASS_CONFIG`   | `StringSerializer` | Serializes message keys as strings    |
| `VALUE_SERIALIZER_CLASS_CONFIG` | `JsonSerializer`   | Serializes message values as JSON     |
| `ACKS_CONFIG`                   | `"all"`            | Waits for all replicas to acknowledge |
| `RETRIES_CONFIG`                | `3`                | Number of retry attempts              |
| `ENABLE_IDEMPOTENCE_CONFIG`     | `true`             | Prevents duplicate messages           |
| `ADD_TYPE_INFO_HEADERS`         | `false`            | Disables type metadata in headers     |

**Reliability Features:**

- ✅ **Idempotent Producer** - Prevents duplicate messages even with retries
- ✅ **Acknowledge All** - Ensures messages are replicated before acknowledgment
- ✅ **Automatic Retries** - Retries up to 3 times on failure
- ✅ **Async Publishing** - Non-blocking event publishing

---

## 🚀 Event Publishing Flow

### Complete Flow Diagram

```
Transaction Service
  │
  ├─ User creates transaction
  │     ↓
  ├─ TransactionService.createTransaction()
  │     ├─ Validate input
  │     ├─ Save to PostgreSQL
  │     ├─ Generate reference number
  │     └─ Build TransactionCreatedEvent
  │           ↓
  ├─ KafkaProducerService.publishTransactionCreatedEvent()
  │     ├─ Create CompletableFuture
  │     ├─ kafkaTemplate.send(topic, key, event)
  │     └─ Add success/failure callback
  │           ↓
  ├─ KafkaTemplate (Spring Kafka)
  │     ├─ Serialize key (String)
  │     ├─ Serialize value (JSON)
  │     └─ Send to Kafka broker
  │           ↓
  ├─ Kafka Broker
  │     ├─ Route to partition based on key
  │     ├─ Replicate to followers (acks=all)
  │     └─ Store message
  │           ↓
  └─ Event Available for Consumers
       (Future: Notification Service, Analytics Service, etc.)
```

### Detailed Publishing Steps

**1. Event Creation:**

```java
TransactionCreatedEvent event = TransactionCreatedEvent.builder()
    .eventId(UUID.randomUUID().toString())
    .eventTimestamp(LocalDateTime.now())
    .transactionId(transaction.getId())
    .userId(transaction.getUserId())
    .userEmail("user-" + transaction.getUserId() + "@fintrack.com")
    .amount(transaction.getAmount())
    .type(transaction.getType())
    .category(transaction.getCategory())
    .description(transaction.getDescription())
    .transactionDate(transaction.getTransactionDate())
    .merchant(transaction.getMerchant())
    .referenceNumber(transaction.getReferenceNumber())
    .createdAt(transaction.getCreatedAt())
    .build();
```

**2. Async Publishing:**

```java
CompletableFuture<SendResult<String, TransactionCreatedEvent>> future =
    kafkaTemplate.send(
        transactionCreatedTopic,           // Topic name
        event.getTransactionId().toString(), // Message key
        event                                // Message value
    );
```

**3. Success/Failure Callback:**

```java
future.whenComplete((result, ex) -> {
    if (ex == null) {
        // Success: Log metadata (topic, partition, offset)
        log.info("Successfully published event: eventId={} to topic={} partition={} offset={}",
            event.getEventId(),
            result.getRecordMetadata().topic(),
            result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
    } else {
        // Failure: Log error (transaction still saved to DB)
        log.error("Failed to publish event: eventId={}, error={}",
            event.getEventId(), ex.getMessage(), ex);
    }
});
```

### Error Handling

**Design Philosophy:**

- Event publishing is **non-blocking** and **best-effort**
- Transaction creation **succeeds even if event publishing fails**
- Failures are logged but don't affect the main transaction flow

**Failure Scenarios:**

1. **Kafka broker unavailable** - Error logged, transaction still saved
2. **Serialization error** - Exception caught and logged
3. **Network timeout** - Retry up to 3 times, then error logged
4. **Topic doesn't exist** - Auto-created if `AUTO_CREATE_TOPICS_ENABLE=true`

---

## 🏗️ Architecture Components

### Component Diagram

```
┌─────────────────────────────────────┐
│    TransactionService               │
│    - Creates transactions            │
│    - Builds events                   │
│    - Calls KafkaProducerService      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    KafkaProducerService             │
│    - Publishes events                │
│    - Handles async callbacks         │
│    - Error logging                   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    KafkaTemplate                    │
│    (Spring Kafka)                    │
│    - Serializes messages             │
│    - Sends to Kafka                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    Kafka Broker                     │
│    - Stores events                   │
│    - Manages partitions              │
│    - Handles replication             │
└─────────────────────────────────────┘
```

### Key Classes

**1. TransactionCreatedEvent**

- **Location:** `com.fintrack.transactionservice.event.TransactionCreatedEvent`
- **Purpose:** Event data structure
- **Features:** Builder pattern for easy construction

**2. KafkaProducerService**

- **Location:** `com.fintrack.transactionservice.service.KafkaProducerService`
- **Purpose:** Encapsulates Kafka publishing logic
- **Features:**
  - Async event publishing
  - Success/failure callbacks
  - Error logging

**3. KafkaProducerConfig**

- **Location:** `com.fintrack.transactionservice.config.KafkaProducerConfig`
- **Purpose:** Spring configuration for Kafka producer
- **Features:**
  - Producer factory configuration
  - KafkaTemplate bean creation
  - Serialization settings

**4. TransactionService**

- **Location:** `com.fintrack.transactionservice.service.TransactionService`
- **Integration:** Calls KafkaProducerService after saving transaction
- **Error Handling:** Catches exceptions to prevent transaction failure

---

## 🔧 Configuration

### Application Configuration

**application.yml:**

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true

kafka:
  topic:
    transaction-created: transaction-created-events
```

### Environment Variables

| Variable                  | Default          | Description            |
| ------------------------- | ---------------- | ---------------------- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker addresses |

### Docker Compose Configuration

**Kafka Service:**

```yaml
kafka:
  image: confluentinc/cp-kafka:7.5.0
  container_name: fintrack-kafka
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
  ports:
    - "9092:9092"
    - "29092:29092"
```

**Key Settings:**

- `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"` - Auto-creates topics on first message
- `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1` - Single broker setup (dev only)
- `PLAINTEXT_HOST://localhost:9092` - Allows external connections
- `PLAINTEXT://kafka:29092` - Internal Docker network connections

---

## 📊 Message Format Details

### Message Structure

**Topic:** `transaction-created-events`

**Partitioning Strategy:**

- **Key-based partitioning** - Uses `transactionId` as key
- **Benefit:** Events for the same transaction are ordered
- **Partition Assignment:** `hash(transactionId) % partitionCount`

**Message Headers:**

- No custom headers by default
- Type information headers disabled (`ADD_TYPE_INFO_HEADERS: false`)

**Serialization:**

- **Key:** UTF-8 String (e.g., "1", "42", "123")
- **Value:** JSON (serialized TransactionCreatedEvent)

### Example Message

**Topic:** `transaction-created-events`
**Partition:** 0 (determined by key hash)
**Offset:** 42
**Key:** `"1"`
**Value:**

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventTimestamp": "2025-12-13T01:10:30.123456",
  "transactionId": 1,
  "userId": 7,
  "userEmail": "jlin18@gmail.com",
  "amount": 125.5,
  "type": "EXPENSE",
  "category": "GROCERIES",
  "description": "Weekly shopping with Kafka",
  "transactionDate": "2024-12-10",
  "merchant": "Whole Foods",
  "referenceNumber": "TXN-D3D6D847",
  "createdAt": "2025-12-13T01:10:30.123456"
}
```

---

## 🔄 Integration Points

### Current Integration

**Transaction Service → Kafka:**

- ✅ Transaction creation triggers event publishing
- ✅ Event published asynchronously (non-blocking)
- ✅ Transaction saved to database regardless of event publishing success

### Future Consumer Services

**Potential Event Consumers:**

1. **Notification Service**

   - Subscribe to `transaction-created-events`
   - Send email/SMS notifications on transaction creation
   - Send balance alerts

2. **Analytics Service**

   - Subscribe to `transaction-created-events`
   - Build real-time analytics dashboards
   - Generate spending insights

3. **Budget Service**

   - Subscribe to `transaction-created-events`
   - Track budget usage in real-time
   - Send budget warnings

4. **Audit Service**

   - Subscribe to all transaction events
   - Maintain audit logs
   - Compliance reporting

5. **Reconciliation Service**
   - Subscribe to `transaction-created-events`
   - Match with bank statements
   - Flag discrepancies

---

## 🎯 Key Features

✅ **Event-Driven Architecture** - Loose coupling between services  
✅ **Async Event Publishing** - Non-blocking transaction processing  
✅ **Idempotent Producer** - Prevents duplicate messages  
✅ **Reliable Delivery** - `acks=all` ensures replication  
✅ **Automatic Retries** - 3 retry attempts on failure  
✅ **Key-Based Partitioning** - Ensures ordering per transaction  
✅ **JSON Serialization** - Human-readable event format  
✅ **Error Resilience** - Transaction success independent of event publishing  
✅ **Comprehensive Logging** - Success and failure callbacks  
✅ **Auto-Topic Creation** - Topics created on first message  
✅ **Docker Integration** - Easy local development setup  
✅ **Kafka UI** - Web interface for monitoring

---

## 🔍 Monitoring & Debugging

### Kafka UI

**Access:** `http://localhost:8090`

**Features:**

- View topics and messages
- Monitor consumer lag
- Inspect message content
- View partition details
- Browse message history

### Logging

**Success Log:**

```
INFO  - Successfully published event: eventId=550e8400... to topic=transaction-created-events partition=0 offset=42
```

**Failure Log:**

```
ERROR - Failed to publish event: eventId=550e8400..., error=Connection refused
```

**Event Publishing Log:**

```
INFO  - Publishing transaction created event: eventId=550e8400..., transactionId=1, userId=7, amount=125.50
```

### Command Line Tools

**List Topics:**

```bash
docker exec -it fintrack-kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Consume Messages:**

```bash
docker exec -it fintrack-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic transaction-created-events \
  --from-beginning
```

**Describe Topic:**

```bash
docker exec -it fintrack-kafka kafka-topics --describe \
  --bootstrap-server localhost:9092 \
  --topic transaction-created-events
```

---

## 🚀 Next Steps

### Short Term

- [ ] Create consumer service template
- [ ] Add transaction updated events
- [ ] Add transaction deleted events
- [ ] Implement event versioning
- [ ] Add event schema registry

### Medium Term

- [ ] Create Notification Service consumer
- [ ] Create Analytics Service consumer
- [ ] Add event replay capability
- [ ] Implement dead letter queue (DLQ)
- [ ] Add event ordering guarantees

### Long Term

- [ ] Multi-region Kafka cluster
- [ ] Event sourcing implementation
- [ ] CQRS pattern with Kafka
- [ ] Real-time streaming analytics
- [ ] Event-driven microservices orchestration

---

## 📝 Best Practices

### Event Design

1. **Immutable Events** - Events should not be modified after creation
2. **Event Versioning** - Include event version for schema evolution
3. **Unique Event IDs** - Use UUIDs for event identification
4. **Timestamp Fields** - Include both event and transaction timestamps
5. **Complete Data** - Include all necessary data for consumers

### Producer Design

1. **Async Publishing** - Never block transaction processing
2. **Idempotency** - Enable idempotent producer for at-least-once delivery
3. **Error Handling** - Log errors but don't fail main transaction
4. **Key Selection** - Choose keys that ensure proper ordering
5. **Monitoring** - Log success/failure with metadata

### Configuration

1. **Acknowledgment** - Use `acks=all` for critical events
2. **Retries** - Configure appropriate retry count
3. **Idempotence** - Always enable for deduplication
4. **Serialization** - Use efficient serialization (JSON for readability)
5. **Topic Naming** - Use clear, consistent naming conventions

---

## 🔐 Security Considerations

### Current Setup

- **Security:** PLAINTEXT protocol (dev only)
- **Authentication:** None (local development)
- **Authorization:** Topic-level permissions not configured

### Production Recommendations

- [ ] Enable SASL/SCRAM authentication
- [ ] Enable SSL/TLS encryption
- [ ] Configure ACLs (Access Control Lists)
- [ ] Use Kafka's built-in security features
- [ ] Implement network isolation
- [ ] Enable audit logging

---

## 📚 Dependencies

### Maven Dependencies

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Version:** Inherited from Spring Boot parent (3.2.0)

**Includes:**

- Apache Kafka client libraries
- Spring Kafka abstractions
- JSON serialization support

---

## 🔗 Related Documentation

- [Transaction Service Summary](./TRANSACTION_SERVICE_SUMMARY.md)
- [User Service Summary](./USER_SERVICE_SUMMARY.md)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)

---

## 📋 Summary

The Kafka implementation provides an **event-driven architecture** foundation for the FinTrack application. When a transaction is created:

1. Transaction is saved to PostgreSQL
2. `TransactionCreatedEvent` is built with all transaction data
3. Event is published to Kafka topic `transaction-created-events`
4. Publishing happens asynchronously (non-blocking)
5. Success/failure is logged for monitoring
6. Future services can consume these events for notifications, analytics, etc.

This design enables **loose coupling**, **scalability**, and **extensibility** - new features can be added by simply adding new consumers without modifying the transaction service.
