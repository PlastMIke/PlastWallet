# 🔐 PlastWallet - Учётные данные пользователей

## Данные для входа

| Пользователь | Email | Пароль | Кошелёк | Баланс |
|--------------|-------|--------|---------|--------|
| **Alice Johnson** | alice@plastwallet.com | password123 | USD | $100.00 |
| **Bob Smith** | bob@plastwallet.com | password123 | EUR | €100.00 |
| **Charlie Brown** | charlie@plastwallet.com | password123 | GBP | £100.00 |

---

## 🌐 Вход через веб-интерфейс

1. Откройте **http://localhost:8080/**
2. Введите email и пароль
3. Нажмите **Войти**
4. Просматривайте баланс, историю транзакций

---

## 📡 Вход через API

### Alice (USD)

```bash
# Логин
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}'

# Получить кошелёк
curl http://localhost:8080/api/v1/wallets/user/1 \
  -H "Authorization: Bearer <TOKEN>"
```

### Bob (EUR)

```bash
# Логин
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@plastwallet.com","password":"password123"}'

# Получить кошелёк
curl http://localhost:8080/api/v1/wallets/user/2 \
  -H "Authorization: Bearer <TOKEN>"
```

### Charlie (GBP)

```bash
# Логин
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"charlie@plastwallet.com","password":"password123"}'

# Получить кошелёк
curl http://localhost:8080/api/v1/wallets/user/3 \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 📊 Проверка через базу данных

```bash
docker exec -it wallet-postgres psql -U postgres -d wallet_db -c "
SELECT 
  w.user_id as ID,
  CASE w.user_id
    WHEN 1 THEN 'Alice Johnson'
    WHEN 2 THEN 'Bob Smith'
    WHEN 3 THEN 'Charlie Brown'
  END as Name,
  u.email as Email,
  w.currency as Currency,
  w.balance as Balance
FROM wallets w
JOIN users u ON w.user_id = u.id
ORDER BY w.user_id;
"
```

---

## 🎯 Быстрый доступ

### Веб-интерфейс
- **URL:** http://localhost:8080/
- **Swagger:** http://localhost:8080/swagger-ui.html

### Переводы между пользователями

Чтобы сделать перевод:
1. Залогиньтесь как отправитель
2. Нажмите **"Перевод"**
3. Введите ID кошелька получателя
4. Введите сумму (например, 50)
5. Нажмите **"Перевести"**

---

## ⚠️ Важно

- Пароль одинаковый для всех: `password123`
- Для тестирования переводов нужны 2 разных пользователя
- ID кошельков можно узнать через API или базу данных
