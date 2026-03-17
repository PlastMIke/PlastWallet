#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================="
echo "🔐 PlastWallet - Логин пользователей"
echo "========================================="

# Alice
echo -e "\n👤 Alice Johnson (alice@plastwallet.com)"
echo "----------------------------------------"
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')
TOKEN=$(echo $RESP | jq -r '.data.token')
echo "Token: ${TOKEN:0:80}..."
echo "Balance:"
curl -s "$BASE_URL/api/v1/wallets/user/1" \
  -H "Authorization: Bearer $TOKEN" | jq '.data'

# Bob
echo -e "\n👤 Bob Smith (bob@plastwallet.com)"
echo "----------------------------------------"
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@plastwallet.com","password":"password123"}')
TOKEN=$(echo $RESP | jq -r '.data.token')
echo "Token: ${TOKEN:0:80}..."
echo "Balance:"
curl -s "$BASE_URL/api/v1/wallets/user/2" \
  -H "Authorization: Bearer $TOKEN" | jq '.data'

# Charlie
echo -e "\n👤 Charlie Brown (charlie@plastwallet.com)"
echo "----------------------------------------"
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"charlie@plastwallet.com","password":"password123"}')
TOKEN=$(echo $RESP | jq -r '.data.token')
echo "Token: ${TOKEN:0:80}..."
echo "Balance:"
curl -s "$BASE_URL/api/v1/wallets/user/3" \
  -H "Authorization: Bearer $TOKEN" | jq '.data'

echo -e "\n========================================="
echo "✅ Готово!"
echo "========================================="
