# Wallet Service Documentation Index

This folder contains complete documentation for the Wallet Service project.

---

## 📚 Available Documents

| File                                                   | Description | Size |
|--------------------------------------------------------|-------------|------|
| [`ИНДЕКС_ДОКУМЕНТАЦИИ.md`](./PROJECT_DOCUMENTATION.md) | Complete project reference documentation | ~17 KB |
| [`ИСТОРИЯ_РАЗРАБОТКИ.md`](./DEVELOPMENT_LOG.md)        | Detailed development session log | ~14 KB |

---

## 📖 Quick Reference

### For New Developers
Start with **ИНДЕКС_ДОКУМЕНТАЦИИ.md** - contains:
- Project structure
- Architecture overview
- Getting started guide
- API documentation

### For Project Maintainers
Review **ИСТОРИЯ_РАЗРАБОТКИ.md** - contains:
- Complete session timeline
- All changes made
- Issues and resolutions
- Build history

---

## 🔗 Related Files

| Location | Description |
|----------|-------------|
| `src/main/resources/db/migration/V1__*.sql` | Database schema with indexes |
| `src/main/resources/db/optimization-report.md` | SQL optimization details |
| `src/main/resources/application.properties` | Application configuration |
| `pom.xml` | Maven dependencies |

---

## 📊 Project Summary

| Metric | Value |
|--------|-------|
| Total Files Created | 47 |
| Lines of Code | ~2,500 |
| Database Indexes | 17 |
| API Endpoints | 7 |
| Repository Queries | 34 |
| Build Status | ✅ SUCCESS |

---

## 🚀 Quick Start

```bash
# 1. Start PostgreSQL
docker run --name wallet-db \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=wallet_db \
  -p 5432:5432 \
  -d postgres:15

# 2. Run application
./mvnw spring-boot:run

# 3. Test API
curl http://localhost:8080/api/v1/wallets
```

---

*Documentation last updated: 2026-03-11*
