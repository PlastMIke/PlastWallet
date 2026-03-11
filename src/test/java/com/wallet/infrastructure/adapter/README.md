# Wallet Service - Integration Tests with Testcontainers

## 📋 Overview

This directory contains integration tests that use **Testcontainers** to spin up real PostgreSQL databases for testing. These tests provide high confidence that the application works correctly with a real database.

## 🐳 Prerequisites

- **Docker** or **Podman** installed and running
- Maven 3.8+
- Java 17+

## 🚀 Running Integration Tests

### Run All Integration Tests

```bash
# Run only integration tests
./mvnw test -Dtest='**/*IntegrationTest.java'

# Or run all tests (unit + integration)
./mvnw test
```

### Run Specific Integration Test

```bash
# Wallet integration tests
./mvnw test -Dtest=WalletIntegrationTest

# Transaction repository integration tests
./mvnw test -Dtest=TransactionRepositoryIntegrationTest
```

### Run with Docker Profile

```bash
# Activate docker profile for integration tests
./mvnw test -Pdocker
```

## 📦 Test Classes

### 1. WalletIntegrationTest

Tests the complete wallet workflow including:
- Wallet creation
- Deposit operations
- Withdrawal operations
- Transfer between wallets
- Transaction history retrieval

**Requirements:**
- Docker for PostgreSQL container
- ~30 seconds to start (pulls PostgreSQL image if needed)

### 2. TransactionRepositoryIntegrationTest

Tests the transaction repository layer with:
- Find operations (by ID, wallet, status, date range)
- Count operations
- Aggregation queries (SUM, totals)
- Pagination support

## 🔧 Configuration

The tests use the following Testcontainers configuration:

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("wallet_test")
        .withUsername("test")
        .withPassword("test");
```

**Database Details:**
- Image: `postgres:15-alpine`
- Database: `wallet_test`
- Username: `test`
- Password: `test`

## 📊 Coverage

Integration tests cover:
- ✅ Database schema validation
- ✅ JPA entity mappings
- ✅ Repository query methods
- ✅ Transaction management
- ✅ Business logic with real database
- ✅ Constraint validation

## 🐛 Troubleshooting

### Docker Not Available

If Docker is not available, the tests will fail with:
```
Could not find a valid Docker environment
```

**Solution:** Start Docker Desktop or install Docker.

### Container Start Failure

If containers fail to start:
```
IllegalStateException: Container startup failed
```

**Solutions:**
1. Check Docker is running: `docker ps`
2. Increase Docker memory allocation
3. Clear Docker cache: `docker system prune`

### Port Conflicts

If ports are in use:
```
Bind for 0.0.0.0:5432 failed: port is already allocated
```

**Solution:** Testcontainers uses random ports by default, so this shouldn't occur.

## 📈 Performance

| Test Class | Number of Tests | Average Run Time |
|------------|----------------|------------------|
| WalletIntegrationTest | 12 | ~15 seconds |
| TransactionRepositoryIntegrationTest | 15 | ~18 seconds |

**Total:** 27 tests in ~30 seconds (with Docker image cached)

## 🔗 References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Testcontainers JUnit Jupiter Module](https://www.testcontainers.org/modules/junit_jupiter/)
- [PostgreSQL Container](https://www.testcontainers.org/modules/databases/postgres/)
