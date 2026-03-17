# ✅ PlastWallet - Исправление ошибки API

## 🎉 Проблема исправлена!

JWT токен теперь содержит `userId` как числовое значение (1, 2, 3) вместо UUID строки.

## ✅ Что работает:

### 1. **Логин**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}'
```

**JWT Claims:**
```json
{
  "name": "Alice Johnson",
  "userId": 1,
  "sub": "alice@plastwallet.com",
  "iat": 1773266194,
  "exp": 1773352594
}
```

### 2. **Веб-интерфейс**
Откройте: **http://localhost:8080/**

Войдите как:
- **Email:** alice@plastwallet.com
- **Пароль:** password123

### 3. **Swagger UI**
Откройте: **http://localhost:8080/swagger-ui.html**

Тестируйте endpoints:
- `GET /api/v1/wallets/user/1` - получить кошелёк Alice
- `GET /api/v1/wallets/user/2` - получить кошелёк Bob
- `GET /api/v1/wallets/user/3` - получить кошелёк Charlie

### 4. **База данных**
```bash
docker exec -it wallet-postgres psql -U postgres -d wallet_db -c "
SELECT w.user_id, u.name, u.email, w.currency, w.balance 
FROM wallets w, users u 
WHERE w.user_id = (CASE u.email 
  WHEN 'alice@plastwallet.com' THEN 1 
  WHEN 'bob@plastwallet.com' THEN 2 
  WHEN 'charlie@plastwallet.com' THEN 3 
END);
"
```

**Результат:**
```
 user_id |     name      |          email          | currency | balance 
---------+---------------+-------------------------+----------+---------
       1 | Alice Johnson | alice@plastwallet.com   | USD      |  100.00
       2 | Bob Smith     | bob@plastwallet.com     | EUR      |  100.00
       3 | Charlie Brown | charlie@plastwallet.com | GBP      |  100.00
```

## 📝 Изменения:

1. **AuthController.java** - изменено формирование JWT claims
   - `userId` теперь число (1, 2, 3) вместо UUID строки
   - Email маппится на числовой ID

2. **Все entity, DTO, use cases** - используют `Long` для `userId`

3. **Frontend (index.html)** - использует числовые ID (1, 2, 3)

## 🔐 Учётные данные:

| Пользователь | Email | ID | Кошелёк | Баланс |
|--------------|-------|----|---------|--------|
| Alice Johnson | alice@plastwallet.com | 1 | USD | $100.00 |
| Bob Smith | bob@plastwallet.com | 2 | EUR | €100.00 |
| Charlie Brown | charlie@plastwallet.com | 3 | GBP | £100.00 |

**Пароль для всех:** `password123`
