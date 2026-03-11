# Wallet Service - Docker Compose Quick Start

## 🚀 Quick Start

### Start All Services

```bash
# Start in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f kafka
docker-compose logs -f redis
```

### Stop All Services

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (WARNING: deletes data!)
docker-compose down -v
```

## 📊 Services

| Service | Container Name | Port | Purpose |
|---------|---------------|------|---------|
| **app** | wallet-service | 8080 | Wallet Service Application |
| **postgres** | wallet-postgres | 5432 | PostgreSQL Database |
| **redis** | wallet-redis | 6379 | Redis Cache |
| **kafka** | wallet-kafka | 9092 | Kafka Message Broker |
| **zookeeper** | wallet-zookeeper | 2181 | Zookeeper (for Kafka) |

## 🔍 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Application** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **PostgreSQL** | localhost:5432 | postgres/postgres |
| **Redis** | localhost:6379 | - |
| **Kafka** | localhost:9092 | - |

## 🧪 Testing

### Check Service Health

```bash
# Application health
curl http://localhost:8080/actuator/health

# PostgreSQL connection
docker-compose exec postgres pg_isready -U postgres

# Redis connection
docker-compose exec redis redis-cli ping

# Kafka connection
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Access Service Shells

```bash
# PostgreSQL CLI
docker-compose exec postgres psql -U postgres -d wallet_db

# Redis CLI
docker-compose exec redis redis-cli

# Kafka topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Application shell
docker-compose exec app sh
```

## 🛠️ Common Commands

### Build and Start

```bash
# Build images and start
docker-compose up -d --build

# Force rebuild
docker-compose up -d --build --force-recreate
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart app
```

### View Status

```bash
# Container status
docker-compose ps

# Resource usage
docker stats

# Disk usage
docker system df
```

## 📝 Configuration

### Environment Variables

Create `.env` file (copy from `.env.example`):

```bash
# Database
DATABASE_PASSWORD=your_password

# Redis
REDIS_PASSWORD=your_password

# JWT
JWT_SECRET=your_secret
```

### Modify Service Configuration

Edit `docker-compose.yml`:

```yaml
services:
  app:
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - JAVA_OPTS=-Xmx1g -Xms512m
```

## 🐛 Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose logs app

# Check if port is in use
lsof -i :8080
lsof -i :5432
lsof -i :6379
lsof -i :9092

# Free up ports
kill -9 <PID>
```

### Database Connection Error

```bash
# Wait for database to be ready
docker-compose logs -f postgres

# Check database is healthy
docker-compose ps postgres

# Restart database
docker-compose restart postgres
```

### Kafka Connection Error

```bash
# Wait for Kafka to initialize (can take 1-2 minutes)
docker-compose logs -f kafka

# Check Zookeeper is running
docker-compose ps zookeeper

# Restart Kafka
docker-compose restart kafka
```

### Out of Memory

```bash
# Check memory usage
docker stats

# Increase JVM memory in docker-compose.yml
# Edit: JAVA_OPTS=-XX:MaxRAMPercentage=75.0
```

### Clean Up

```bash
# Remove stopped containers
docker-compose down

# Remove orphaned containers
docker-compose down --remove-orphans

# Remove all Docker resources
docker system prune -a --volumes
```

## 📊 Resource Requirements

| Service | CPU | Memory | Disk |
|---------|-----|--------|------|
| app | 1-2 cores | 512MB-2GB | 500MB |
| postgres | 1 core | 512MB-1GB | 10GB |
| redis | 0.5 core | 128MB-512MB | 1GB |
| kafka | 1-2 cores | 1GB-2GB | 5GB |
| zookeeper | 0.5 core | 256MB | 1GB |
| **Total** | **4-6 cores** | **2.5-6GB** | **~18GB** |

## 🎯 Next Steps

1. **Start services:** `docker-compose up -d`
2. **Verify health:** `docker-compose ps`
3. **Access Swagger:** http://localhost:8080/swagger-ui.html
4. **Test API:** Create wallet, deposit, withdraw, transfer

---

**Documentation:** See `docker/README.md` for detailed guide  
**Support:** Contact wallet-service-team@example.com
