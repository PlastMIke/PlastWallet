#!/bin/bash

echo "🔍 Отладка API..."

# 1. Login
echo -e "\n1️⃣ Login..."
RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')
TOKEN=$(echo "$RESP" | jq -r '.data.token')
echo "Token получен: ${TOKEN:0:50}..."

# 2. Пробуем получить все кошельки (если есть endpoint)
echo -e "\n2️⃣ Получение кошелька по UUID..."
UUID="3f672c1a-71c4-424d-b650-eaf9ecbdba89"
echo "GET /api/v1/wallets/user/$UUID"
curl -s -w "\nHTTP Status: %{http_code}\n" "http://localhost:8080/api/v1/wallets/user/$UUID" \
  -H "Authorization: Bearer $TOKEN"

# 3. Проверяем health
echo -e "\n3️⃣ Health check..."
curl -s http://localhost:8080/actuator/health | jq .

echo -e "\n✅ Тест завершён!"
