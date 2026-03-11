# Wallet Service - OpenAPI 3.0 Documentation

## 📋 Overview

This directory contains the complete OpenAPI 3.0 specification for the Wallet Service API.

## 📁 Files

| File | Description |
|------|-------------|
| `openapi-spec.yaml` | Complete OpenAPI 3.0 specification |
| `README.md` | This documentation file |

## 🚀 Quick Start

### View Swagger UI

Once the application is running:

```bash
# Start application
./mvnw spring-boot:run

# Open Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Download OpenAPI Spec

```bash
# JSON format
curl http://localhost:8080/v3/api-docs -o openapi.json

# YAML format
curl http://localhost:8080/v3/api-docs.yaml -o openapi.yaml
```

### Validate OpenAPI Spec

```bash
# Using swagger-cli
npm install -g swagger-cli
swagger-cli validate openapi-spec.yaml

# Using spectral
npm install -g @stoplight/spectral
spectral lint openapi-spec.yaml
```

## 📊 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Register new user | ❌ |
| POST | `/auth/login` | Login user | ❌ |

### Wallets

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/wallets` | Create wallet | ✅ |
| GET | `/wallets/{walletId}` | Get wallet by ID | ✅ |
| GET | `/wallets/user/{userId}` | Get wallet by user | ✅ |
| DELETE | `/wallets/{walletId}` | Delete wallet | ✅ |
| POST | `/wallets/{walletId}/deposit` | Deposit funds | ✅ |
| POST | `/wallets/{walletId}/withdraw` | Withdraw funds | ✅ |
| POST | `/wallets/{fromWalletId}/transfer` | Transfer funds | ✅ |
| GET | `/wallets/{walletId}/transactions` | Transaction history | ✅ |

### Transactions

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/transactions/{transactionId}` | Get transaction | ✅ |
| GET | `/transactions/wallet/{walletId}` | By wallet | ✅ |
| GET | `/transactions/wallet/{walletId}/status/{status}` | By wallet + status | ✅ |
| GET | `/transactions/date-range` | By date range | ✅ |
| GET | `/transactions/amount-range` | By amount range | ✅ |
| GET | `/transactions/stats/wallet/{walletId}` | Wallet statistics | ✅ |
| GET | `/transactions/recent` | Recent transactions | ✅ |

## 🔐 Authentication

All endpoints (except `/auth/**`) require JWT Bearer token.

### Include Token in Request

```bash
curl -X GET http://localhost:8080/api/v1/wallets/{walletId} \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Get Token

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# Response includes token
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

## 📝 Request/Response Examples

### Create Wallet

**Request:**
```json
POST /api/v1/wallets
{
  "userId": 1,
  "currency": "USD"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Wallet created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "userId": 1,
    "currency": "USD",
    "balance": 0.00,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  "statusCode": 201
}
```

### Deposit Funds

**Request:**
```json
POST /api/v1/wallets/{walletId}/deposit
{
  "amount": 100.00,
  "description": "Initial deposit"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Deposit completed successfully",
  "data": {
    "id": "15c5713b-b915-4ba3-a101-a7ee931898ac",
    "fromWalletId": null,
    "toWalletId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 100.00,
    "status": "COMPLETED",
    "createdAt": "2024-01-01T12:00:00Z"
  },
  "statusCode": 200
}
```

### Transfer Funds

**Request:**
```json
POST /api/v1/wallets/{fromWalletId}/transfer
{
  "toWalletId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 25.00,
  "description": "Payment for services"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Transfer completed successfully",
  "data": {
    "id": "abc123-def456",
    "fromWalletId": "550e8400-e29b-41d4-a716-446655440000",
    "toWalletId": "550e8400-e29b-41d4-a716-446655440001",
    "amount": 25.00,
    "status": "COMPLETED",
    "createdAt": "2024-01-01T13:00:00Z"
  },
  "statusCode": 200
}
```

## ❌ Error Responses

### 400 Bad Request

```json
{
  "success": false,
  "message": "Invalid amount (must be positive)",
  "statusCode": 400
}
```

### 401 Unauthorized

```json
{
  "success": false,
  "message": "Invalid or missing authentication token",
  "statusCode": 401
}
```

### 404 Not Found

```json
{
  "success": false,
  "message": "Wallet not found",
  "statusCode": 404
}
```

### 422 Validation Error

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required",
      "rejectedValue": null
    },
    {
      "field": "amount",
      "message": "Amount must be greater than 0",
      "rejectedValue": -50
    }
  ],
  "statusCode": 422
}
```

## 🔧 Configuration

### Springdoc OpenAPI Config

```properties
# application.properties
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.api-docs.enabled=true
springdoc.packages-to-scan=com.wallet.interfaces.api
springdoc.paths-to-match=/api/**
```

### Custom OpenAPI Bean

```java
@Bean
public OpenAPI walletServiceAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Wallet Service API")
            .version("v1.0.0")
            .description("Digital Wallet Service API"))
        .addSecurityItem(new SecurityRequirement()
            .addList("bearerAuth"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
}
```

## 📈 Rate Limiting

| Tier | Requests/Second | Requests/Minute |
|------|-----------------|-----------------|
| Default | 10 | 100 |
| Authenticated | 100 | 1000 |
| Admin | 500 | 5000 |

## 🎯 Best Practices

1. **Always use HTTPS** in production
2. **Include Authorization header** for protected endpoints
3. **Handle 429 Too Many Requests** with exponential backoff
4. **Use pagination** for list endpoints
5. **Validate input** on client side before sending
6. **Store tokens securely** (never in localStorage for web apps)
7. **Refresh tokens** before expiration

## 🛠️ Tools

### Generate Client SDK

```bash
# TypeScript
openapi-generator generate -i openapi-spec.yaml -g typescript-axios -o ./client

# Java
openapi-generator generate -i openapi-spec.yaml -g java -o ./client

# Python
openapi-generator generate -i openapi-spec.yaml -g python -o ./client
```

### Generate Server Stub

```bash
# Spring Boot
openapi-generator generate -i openapi-spec.yaml -g spring -o ./server
```

### Test API

```bash
# Using Postman
# Import openapi-spec.yaml into Postman

# Using Insomnia
# Import openapi-spec.yaml into Insomnia

# Using curl
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"userId": 1, "currency": "USD"}'
```

## 📚 References

- [OpenAPI Specification](https://swagger.io/specification/)
- [Springdoc OpenAPI](https://springdoc.org/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
- [OpenAPI Generator](https://openapi-generator.tech/)

## 🎯 Next Steps

1. **View Swagger UI:** http://localhost:8080/swagger-ui.html
2. **Download spec:** `curl http://localhost:8080/v3/api-docs -o openapi.json`
3. **Generate client:** Use openapi-generator
4. **Test endpoints:** Use Swagger UI or Postman

---

**Version:** 1.0.0  
**Last Updated:** 2024-01-01  
**Contact:** support@wallet.service
