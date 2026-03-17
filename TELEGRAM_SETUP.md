# 📱 Настройка Telegram бота через переменные окружения

## 🚀 Быстрая настройка

### 1. Откройте файл `.env`

Файл находится в корне проекта:
```
/Users/plustme/Desktop/PlastWallet/wallet-service/.env
```

### 2. Получите токен бота

1. Откройте @BotFather в Telegram
2. Отправьте `/newbot`
3. Введите имя: `PlastWallet Monitor`
4. Введите username: `PlastWalletTestBot`
5. Скопируйте токен (например: `7234567890:AABxKqL9mN3pQrStUvWxYz1234567890ABC`)

### 3. Получите Chat ID

1. Найдите своего бота в Telegram
2. Нажмите **Start**
3. Откройте в браузере:
   ```
   https://api.telegram.org/bot<ВАШ_ТОКЕН>/getUpdates
   ```
4. Найдите `chat.id` в JSON (например: `123456789`)

### 4. Обновите `.env` файл

Откройте `.env` и замените значения:

```bash
# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=7234567890:AABxKqL9mN3pQrStUvWxYz1234567890ABC
TELEGRAM_BOT_USERNAME=PlastWalletTestBot
TELEGRAM_CHAT_ID=123456789

# Notification settings
TELEGRAM_NOTIFICATIONS_DAILY_REPORT_ENABLED=true
TELEGRAM_NOTIFICATIONS_HEALTH_CHECK_ENABLED=true
TELEGRAM_NOTIFICATIONS_MORNING_SUMMARY_ENABLED=true
```

### 5. Перезапустите сервис

```bash
cd /Users/plustme/Desktop/PlastWallet/wallet-service
docker compose up -d
```

### 6. Проверьте логи

```bash
docker compose logs -f plast-wallet | grep -i telegram
```

Должно появиться:
```
INFO --- Initializing Telegram Bot...
INFO --- Telegram Bot initialized successfully
```

---

## 📋 Структура переменных

| Переменная | Описание | Пример |
|------------|----------|--------|
| `TELEGRAM_BOT_TOKEN` | Токен от @BotFather | `723...:AAB...` |
| `TELEGRAM_BOT_USERNAME` | Username бота | `PlastWalletTestBot` |
| `TELEGRAM_CHAT_ID` | ID чата для уведомлений | `123456789` |
| `TELEGRAM_NOTIFICATIONS_DAILY_REPORT_ENABLED` | Дневные отчёты | `true`/`false` |
| `TELEGRAM_NOTIFICATIONS_HEALTH_CHECK_ENABLED` | Уведомления о здоровье | `true`/`false` |
| `TELEGRAM_NOTIFICATIONS_MORNING_SUMMARY_ENABLED` | Утренние сводки | `true`/`false` |

---

## 🔍 Проверка работы

### Тестовое сообщение через API

```bash
# Замените на ваши значения
TOKEN="7234567890:AABxKqL9mN3pQrStUvWxYz1234567890ABC"
CHAT_ID="123456789"

curl -X GET "https://api.telegram.org/bot${TOKEN}/sendMessage?chat_id=${CHAT_ID}&text=✅ PlastWallet Bot Test"
```

### Проверка через Docker

```bash
# Проверка что переменные загружены
docker compose exec plast-wallet env | grep TELEGRAM

# Логи бота
docker compose logs plast-wallet | grep -i "telegram\|bot"
```

---

## 🔧 Для локальной разработки (без Docker)

### Вариант 1: Через `.env.local`

Создайте файл `.env.local` в корне проекта:

```bash
TELEGRAM_BOT_TOKEN=7234567890:AABxKqL9mN3pQrStUvWxYz1234567890ABC
TELEGRAM_CHAT_ID=123456789
```

Запустите приложение:
```bash
./mvnw spring-boot:run
```

### Вариант 2: Через командную строку

```bash
export TELEGRAM_BOT_TOKEN=7234567890:AABxKqL9mN3pQrStUvWxYz1234567890ABC
export TELEGRAM_CHAT_ID=123456789

./mvnw spring-boot:run
```

---

## ⚠️ Важные заметки

### Безопасность

- ⚠️ **Не коммитьте** файл `.env` в Git!
- ✅ Файл `.env` уже в `.gitignore`
- ✅ Шаблон `.env.example` безопасен для коммита

### Если бот не работает

1. **Проверьте токен:**
   ```bash
   curl https://api.telegram.org/bot<TOKEN>/getMe
   ```

2. **Проверьте Chat ID:**
   ```bash
   curl https://api.telegram.org/bot<TOKEN>/getUpdates
   ```

3. **Убедитесь что нажали Start** в боте

4. **Проверьте логи:**
   ```bash
   docker compose logs plast-wallet | grep -i error
   ```

---

## 📬 Какие уведомления приходят

### 1. Health Check (каждые 5 мин)
Если сервис упал:
```
🚨 ALERT

Name: Service Health Check Failed
Severity: CRITICAL
Description: PlastWallet health status changed to: DOWN
```

Если сервис восстановился:
```
✅ Service Recovered!

PlastWallet is back online and healthy.
```

### 2. Daily Report (ежедневно в 18:00)
```
📊 Daily Report

Date: 2024-03-11
💰 Transactions: 234
💵 Total Amount: $123,456.78
👛 New Wallets: 12
⚠️ Errors: 2
```

### 3. Morning Summary (ежедневно в 09:00)
```
☀️ Good Morning!

PlastWallet is running smoothly.

📊 Overnight Stats:
• Transactions: 45
• Volume: $12,345.67
• New Users: 3
```

---

## ✅ Чек-лист

- [ ] Бот создан в @BotFather
- [ ] Токен сохранён в `.env`
- [ ] Chat ID получен и добавлен в `.env`
- [ ] Нажата кнопка **Start** в боте
- [ ] Сервис перезапущен: `docker compose up -d`
- [ ] В логах: `Telegram Bot initialized successfully`
- [ ] Тестовое сообщение получено

---

**Готово! 🎉**
