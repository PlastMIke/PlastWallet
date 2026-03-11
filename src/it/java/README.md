# 🧪 Integration Tests with Testcontainers

## Overview

This folder contains integration tests that use **Testcontainers** to spin up real PostgreSQL databases for testing.

## Prerequisites

- **Docker** or **Podman** installed and running
- Maven 3.8+
- Java 17+

## Running Integration Tests

The integration tests are excluded from the default test run. To run them:

```bash
# Run integration tests specifically
./mvnw verify -Pintegration

# Or run a specific test
./mvnw verify -Pintegration -Dtest=WalletIntegrationTest
```

## Test Classes

### WalletIntegrationTest

Tests the complete wallet workflow with a real PostgreSQL database:
- ✅ Wallet creation
- ✅ Deposit operations  
- ✅ Withdrawal operations
- ✅ Transfer between wallets
- ✅ Transaction history retrieval

**Configuration:**
- PostgreSQL 15 (Alpine)
- Database: `wallet_test`
- User: `test` / Password: `test`

## How It Works

Testcontainers automatically:
1. Pulls the PostgreSQL Docker image if not present
2. Starts a container before tests
3. Configures the datasource with dynamic connection properties
4. Runs tests against the real database
5. Stops the container after tests

## Troubleshooting

### Docker Not Available

If you see: `Could not find a valid Docker environment`

**Solution:** Start Docker Desktop or ensure Docker daemon is running.

### Slow First Run

First run downloads the PostgreSQL image (~150MB).

**Solution:** Subsequent runs are fast as the image is cached.

## Coverage

| Metric | Value |
|--------|-------|
| Test Classes | 1 |
| Test Methods | 12 |
| Estimated Run Time | ~30 seconds |
| Database | PostgreSQL 15 |

## References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [PostgreSQL Container Module](https://www.testcontainers.org/modules/databases/postgres/)
