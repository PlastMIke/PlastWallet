# Wallet Service - Docker Quick Start Guide

## 📋 Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- 4GB RAM minimum (8GB recommended)
- 10GB disk space

## 🚀 Quick Start

### 1. Development Environment

```bash
# Start all services (app + databases)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

**Access Points:**
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:8081 (admin@admin.com / admin)
- Redis Commander: http://localhost:8082

### 2. Production Environment

```bash
# Create .env file from template
cp .env.example .env

# Edit .env with your production values
nano .env

# Start production stack
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f app
```

## 🔧 Build Commands

### Build Docker Image

```bash
# Build production image
docker build -t wallet-service:latest .

# Build with specific tag
docker build -t wallet-service:0.0.1-SNAPSHOT --build-arg VERSION=0.0.1-SNAPSHOT .

# Build without cache
docker build --no-cache -t wallet-service:latest .
```

### Build Specific Stage

```bash
# Build only runtime stage
docker build --target runtime -t wallet-service:runtime .

# Build debug stage
docker build --target debug -t wallet-service:debug .
```

## 🧪 Testing

### Run Tests in Container

```bash
# Run unit tests
docker run --rm wallet-service:latest java -jar app.jar --spring.profiles.active=test

# Run with Maven in container
docker run --rm -v $(pwd):/build -w /build maven:3.9.6-eclipse-temurin-17 ./mvnw test
```

### Integration Tests

```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d

# Run tests
./mvnw verify

# Stop test environment
docker-compose -f docker-compose.test.yml down
```

## 🔍 Monitoring

### Health Check

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check detailed health
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/health/liveness
```

### View Logs

```bash
# Application logs
docker-compose logs -f app

# Database logs
docker-compose logs -f postgres

# Kafka logs
docker-compose logs -f kafka

# All logs
docker-compose logs -f
```

### Access Database

```bash
# PostgreSQL CLI
docker-compose exec postgres psql -U postgres -d wallet_db

# Redis CLI
docker-compose exec redis redis-cli

# Kafka topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

## 🛠️ Maintenance

### Backup Database

```bash
# Create backup
docker-compose exec postgres pg_dump -U postgres wallet_db > backup_$(date +%Y%m%d).sql

# Restore backup
docker-compose exec -T postgres psql -U postgres wallet_db < backup_20240101.sql
```

### Update Application

```bash
# Pull new image
docker-compose pull

# Recreate containers
docker-compose up -d --force-recreate

# Cleanup old images
docker image prune -f
```

### Scale Application

```bash
# Scale to 3 instances (requires load balancer)
docker-compose up -d --scale app=3
```

## 🐛 Troubleshooting

### Container Won't Start

```bash
# Check logs
docker-compose logs app

# Check resource usage
docker stats

# Check disk space
docker system df
```

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Test connection
docker-compose exec postgres pg_isready

# Check database logs
docker-compose logs postgres
```

### Memory Issues

```bash
# Check container memory
docker stats --no-stream

# Increase JVM memory in docker-compose.yml
# Edit: JAVA_OPTS=-XX:MaxRAMPercentage=75.0
```

### Clean Up

```bash
# Remove stopped containers
docker-compose down

# Remove volumes (WARNING: deletes data!)
docker-compose down -v

# Remove all Docker resources
docker system prune -a --volumes
```

## 📊 Resource Requirements

| Service | CPU | Memory | Disk |
|---------|-----|--------|------|
| Application | 1-2 cores | 512MB-2GB | 500MB |
| PostgreSQL | 1-2 cores | 512MB-2GB | 10GB |
| Redis | 0.5 core | 128MB-512MB | 1GB |
| Kafka | 1-2 cores | 1GB-2GB | 5GB |
| **Total** | **3.5-6.5** | **2.5-5GB** | **~17GB** |

## 🔐 Security Best Practices

1. **Never commit .env file** - Contains secrets
2. **Use secrets management** - Docker secrets or Vault
3. **Run as non-root** - Already configured in Dockerfile
4. **Enable TLS** - For production database connections
5. **Regular updates** - Keep base images updated
6. **Scan images** - Use `docker scan` or Trivy

```bash
# Scan for vulnerabilities
docker scan wallet-service:latest

# Or use Trivy
trivy image wallet-service:latest
```

## 📝 Environment Variables

See `.env.example` for all available configuration options.

### Required Variables

```bash
# Database
DATABASE_PASSWORD=your_secure_password

# Redis
REDIS_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_jwt_secret_key

# Kafka
KAFKA_HOST=localhost
```

## 🎯 Next Steps

1. **Configure production settings** - Edit `docker-compose.prod.yml`
2. **Set up monitoring** - Prometheus + Grafana
3. **Configure logging** - ELK Stack or similar
4. **Set up CI/CD** - GitHub Actions, GitLab CI, etc.
5. **Enable TLS/SSL** - For production endpoints

---

**Documentation:** See `docs/` folder for detailed guides  
**Support:** Contact wallet-service-team@example.com
