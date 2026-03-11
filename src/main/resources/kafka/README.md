# 📬 Kafka Configuration

## Overview

This wallet service uses **Apache Kafka** for event-driven architecture to:
- Publish transaction events
- Process notifications asynchronously
- Enable microservices communication

## 🏗️ Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  WalletService  │────▶│  Kafka Broker    │────▶│ NotificationSvc │
│                 │     │  (localhost:9092)│     │                 │
└─────────────────┘     └──────────────────┘     └─────────────────┘
       │                       │
       │                       │
       ▼                       ▼
┌─────────────────┐     ┌──────────────────┐
│ TransactionProd │     │ NotificationCons │
└─────────────────┘     └──────────────────┘
```

## 📦 Topics

| Topic Name | Partitions | Description |
|------------|------------|-------------|
| `wallet-transactions` | 3 | Transaction events |
| `wallet-notifications` | 3 | Notification events |

## 🔧 Configuration

### application.properties

```properties
# Kafka Broker
spring.kafka.bootstrap-servers=localhost:9092

# Consumer Configuration
spring.kafka.consumer.group-id=wallet-service-group
spring.kafka.consumer.auto-offset-reset=latest
spring.kafka.consumer.enable-auto-commit=false

# Topics
kafka.topics.transactions=wallet-transactions
kafka.topics.notifications=wallet-notifications
```

## 🚀 Running Kafka

### Option 1: Docker (Recommended)

```bash
# Start Kafka with Zookeeper
docker run -d --name kafka \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -p 9092:9092 \
  confluentinc/cp-kafka:latest

# Or use Kafka in Docker Compose
docker-compose up -d kafka
```

### Option 2: Local Installation

1. Download Kafka from [Apache Kafka](https://kafka.apache.org/downloads)
2. Start Zookeeper: `bin/zookeeper-server-start.sh config/zookeeper.properties`
3. Start Kafka: `bin/kafka-server-start.sh config/server.properties`

## 📝 Usage

### Publishing Transaction Events

```java
@Autowired
private EventPublisherService eventPublisher;

public void processTransaction(TransactionDTO transaction) {
    // Process transaction...
    
    // Publish event
    eventPublisher.publishTransactionEvent(transaction, "Deposit processed");
}
```

### Using Producer Directly

```java
@Autowired
private TransactionProducer producer;

public void sendEvent(TransactionEvent event) {
    producer.sendTransactionEvent(event);
}
```

### Consuming Events

```java
@KafkaListener(
    topics = "wallet-transactions",
    groupId = "wallet-service-group"
)
public void listen(ConsumerRecord<String, Object> record) {
    log.info("Received: {}", record.value());
}
```

## 🧪 Testing

### Unit Test with Embedded Kafka

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"wallet-transactions"})
class KafkaIntegrationTest {

    @Autowired
    private TransactionProducer producer;

    @Test
    void shouldSendEvent() {
        TransactionEvent event = TransactionEvent.builder()
            .transactionId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(100))
            .build();
        
        producer.sendTransactionEvent(event);
        
        // Verify using Kafka consumer
    }
}
```

### Integration Test with Testcontainers

```java
@Testcontainers
@SpringBootTest
class KafkaIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:latest")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
```

## 📊 Monitoring

### View Topics

```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Consume Messages

```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic wallet-transactions \
  --from-beginning
```

### Produce Messages

```bash
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic wallet-transactions
```

## 🔍 Event Schemas

### TransactionEvent

```json
{
  "id": "uuid",
  "transactionId": "uuid",
  "fromWalletId": "uuid",
  "toWalletId": "uuid",
  "amount": 100.00,
  "type": "DEPOSIT|WITHDRAWAL|TRANSFER",
  "status": "COMPLETED|PENDING|FAILED",
  "createdAt": "2024-01-01T00:00:00Z",
  "description": "string"
}
```

### NotificationEvent

```json
{
  "id": "uuid",
  "userId": "uuid",
  "type": "TRANSACTION|ALERT|SYSTEM",
  "title": "string",
  "message": "string",
  "channel": "EMAIL|SMS|PUSH",
  "read": false,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

## 🐛 Troubleshooting

### Connection Refused

```
org.apache.kafka.common.errors.TimeoutException
```

**Solution:** Ensure Kafka is running and accessible at `localhost:9092`

### Topic Not Found

```
org.apache.kafka.common.errors.UnknownTopicOrPartitionException
```

**Solution:** Topics are auto-created on first use, or create manually:
```bash
kafka-topics --create --topic wallet-transactions --bootstrap-server localhost:9092
```

### Deserialization Error

```
org.springframework.kafka.support.serializer.JsonSerializationException
```

**Solution:** Ensure `spring.kafka.consumer.properties.spring.json.trusted.packages` includes your package

## 📚 References

- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Testcontainers Kafka](https://www.testcontainers.org/modules/kafka/)
