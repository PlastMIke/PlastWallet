# 🤖 Telegram Bot для Мониторинга

## 📋 Обзор

Wallet Service теперь включает Telegram бота для:
- 📊 Мониторинга статуса системы
- 🚨 Получения алертов о проблемах
- 📈 Ежедневных отчётов
- 💬 Интерактивных команд

---

## 🚀 Настройка

### Шаг 1: Создать Telegram Бота

1. Откройте Telegram и найдите **@BotFather**
2. Отправьте команду `/newbot`
3. Введите имя бота (например: `Wallet Service Monitor`)
4. Введите username бота (например: `WalletServiceMonitorBot`)
5. **Сохраните токен** (выглядит как: `1234567890:ABCdefGHIjklMNOpqrsTUVwxyz`)

### Шаг 2: Узнать Chat ID

1. Откройте созданного бота
2. Отправьте любое сообщение
3. Перейдите по ссылке: `https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates`
4. Найдите `"chat":{"id":123456789,...}`
5. **Сохраните Chat ID** (число, например: `123456789`)

### Шаг 3: Настроить Приложение

Откройте `application.properties`:

```properties
# Вставьте ваш токен
telegram.bot.token=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz

# Вставьте username бота
telegram.bot.username=WalletServiceMonitorBot

# Вставьте ваш Chat ID
telegram.chat.id=123456789
```

### Шаг 4: Запустить

```bash
./mvnw spring-boot:run
```

---

## 📱 Доступные Команды

| Команда | Описание |
|---------|----------|
| `/start` | Запустить бота |
| `/help` | Показать справку |
| `/status` | Статус системы |
| `/health` | Проверка здоровья |
| `/wallets` | Статистика кошельков |
| `/transactions` | Последние транзакции |
| `/report` | Дневной отчёт |

---

## 🔔 Автоматические Уведомления

### Алерты

Бот автоматически отправляет уведомления при:
- ❌ Падении сервиса (Health Check failed)
- ✅ Восстановлении сервиса
- ⚠️ Критических ошибках
- 💳 Подозрительных транзакциях

### Расписание

| Уведомление | Время | Описание |
|-------------|-------|----------|
| **Ежедневный отчёт** | 23:00 | Статистика за день |
| **Проверка здоровья** | Каждый час | Status check |
| **Утреняя сводка** | 09:00 | Статистика за ночь |

---

## 📊 Примеры Сообщений

### Алерт о Проблеме
```
🔴 🚨 ALERT

Name: Service Health Check Failed
Severity: CRITICAL
Description: Wallet Service health status changed to: DOWN

Time: 2024-01-01T12:00:00Z
```

### Транзакция
```
💰 Transaction

Type: DEPOSIT
Amount: 100.00
Status: COMPLETED
Wallet: `550e8400-e29b-41d4-a716-446655440000`
```

### Дневной Отчёт
```
📊 Daily Report

Date: 2024-01-01

💰 Transactions: 234
💵 Total Amount: $123,456.78
👛 New Wallets: 12
⚠️ Errors: 2
```

### Проверка Здоровья
```
✅ System Health

Status: UP
Uptime: 24h 15m
Active Users: 567
Transactions: 1234
```

---

## 🔧 Расширенная Настройка

### Включить/Выключить Уведомления

```properties
# Ежедневный отчёт
telegram.notifications.daily-report.enabled=true

# Проверка здоровья каждый час
telegram.notifications.health-check.enabled=true

# Утренняя сводка
telegram.notifications.morning-summary.enabled=true
```

### Настроить Расписание (Cron)

```properties
# Ежедневный отчёт в 23:00
telegram.schedule.daily-report=0 0 23 * * *

# Проверка здоровья каждый час
telegram.schedule.health-check=0 0 * * * *

# Утренняя сводка в 09:00
telegram.schedule.morning-summary=0 0 9 * * *
```

---

## 🛠️ Интеграция с AlertService

### Отправить Алерт из Кода

```java
@Autowired
private AlertService alertService;

// Отправить алерт
alertService.sendAlert(
    "High Error Rate",
    "WARNING",
    "Error rate exceeded 5% threshold"
);

// Отправить уведомление о транзакции
alertService.sendTransactionAlert(
    "DEPOSIT",
    "$100.00",
    "COMPLETED",
    walletId.toString()
);
```

### Health Check Monitoring

```java
@Autowired
private AlertService alertService;

@Scheduled(fixedRate = 300000) // 5 минут
public void monitorHealth() {
    HealthComponent health = healthEndpoint.health();
    
    if (!Status.UP.equals(health.getStatus())) {
        alertService.sendAlert(
            "Service Down",
            "CRITICAL",
            health.getDetails().toString()
        );
    }
}
```

---

## 🐛 Troubleshooting

### Бот не Отвечает

```bash
# Проверить логи
grep -i telegram logs/application.log

# Проверить токен
curl "https://api.telegram.org/bot<YOUR_TOKEN>/getMe"

# Проверить Chat ID
curl "https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates"
```

### Ошибки в Логах

```
Failed to send Telegram message: Chat ID not found
```
**Решение:** Проверьте `telegram.chat.id` в application.properties

```
Unauthorized: bot token not valid
```
**Решение:** Проверьте `telegram.bot.token`, получите новый у @BotFather

---

## 📈 Метрики

Отслеживайте метрики бота в Prometheus:

```promql
# Отправленные сообщения
telegram_messages_sent_total

# Ошибки отправки
telegram_errors_total

# Время ответа
telegram_response_time_seconds
```

---

## 🔐 Безопасность

### Рекомендации

1. **Не коммитьте токен в Git!**
   ```bash
   # Используйте переменные окружения
   export TELEGRAM_BOT_TOKEN=your_token
   export TELEGRAM_CHAT_ID=your_chat_id
   ```

2. **Ограничьте доступ**
   - Бот отвечает только admin Chat ID
   - Все остальные запросы блокируются

3. **Используйте Webhook** (для production)
   ```properties
   telegram.webhook.url=https://your-domain.com/api/telegram/webhook
   ```

---

## 🎯 Следующие Шаги

1. ✅ Создать бота через @BotFather
2. ✅ Настроить application.properties
3. ✅ Запустить приложение
4. ✅ Отправить `/start` боту
5. ✅ Протестировать команды
6. ✅ Настроить автоматические уведомления

---

## 📚 Ресурсы

- [Telegram Bot API](https://core.telegram.org/bots/api)
- [Telegram Bots Library](https://github.com/TelegramBots/telegrambots)
- [BotFather](https://t.me/botfather)

---

**Готово!** 🎉 Теперь у вас есть Telegram бот для мониторинга Wallet Service!
