#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================="
echo "💰 PlastWallet - Пополнение и Переводы"
echo "========================================="

# Логинимся как Alice (user 1)
echo -e "\n🔐 Logging in as Alice..."
RESP1=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')
TOKEN1=$(echo $RESP1 | jq -r '.data.token')
echo "Token 1: ${TOKEN1:0:50}..."

# Логинимся как Bob (user 2)
echo -e "\n🔐 Logging in as Bob..."
RESP2=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@plastwallet.com","password":"password123"}')
TOKEN2=$(echo $RESP2 | jq -r '.data.token')
echo "Token 2: ${TOKEN2:0:50}..."

# Логинимся как Charlie (user 3)
echo -e "\n🔐 Logging in as Charlie..."
RESP3=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"charlie@plastwallet.com","password":"password123"}')
TOKEN3=$(echo $RESP3 | jq -r '.data.token')
echo "Token 3: ${TOKEN3:0:50}..."

# Получаем ID кошельков
echo -e "\n📋 Getting wallet IDs..."

# Кошелёк Alice (USD)
W1=$(curl -s "$BASE_URL/api/v1/wallets/user/1" -H "Authorization: Bearer $TOKEN1")
WALLET1_ID=$(echo $W1 | jq -r '.data.id')
echo "Wallet 1 (USD): $WALLET1_ID"

# Кошелёк Bob (EUR)
W2=$(curl -s "$BASE_URL/api/v1/wallets/user/2" -H "Authorization: Bearer $TOKEN2")
WALLET2_ID=$(echo $W2 | jq -r '.data.id')
echo "Wallet 2 (EUR): $WALLET2_ID"

# Кошелёк Charlie (GBP)
W3=$(curl -s "$BASE_URL/api/v1/wallets/user/3" -H "Authorization: Bearer $TOKEN3")
WALLET3_ID=$(echo $W3 | jq -r '.data.id')
echo "Wallet 3 (GBP): $WALLET3_ID"

echo -e "\n========================================="
echo "💵 Все кошельки уже пополнены на \$100/€100/£100"
echo "========================================="

echo -e "\n========================================="
echo "🔄 Выполняем переводы по $50..."
echo "========================================="

# Alice -> Bob: $50
echo -e "\n🔄 Alice → Bob: \$50..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET1_ID/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d "{\"toWalletId\":\"$WALLET2_ID\",\"amount\":50,\"description\":\"Payment for services\"}" | jq -r '.message'

# Bob -> Charlie: €50
echo -e "\n🔄 Bob → Charlie: €50..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET2_ID/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d "{\"toWalletId\":\"$WALLET3_ID\",\"amount\":50,\"description\":\"Refund for dinner\"}" | jq -r '.message'

# Charlie -> Alice: £50
echo -e "\n🔄 Charlie → Alice: £50..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET3_ID/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN3" \
  -d "{\"toWalletId\":\"$WALLET1_ID\",\"amount\":50,\"description\":\"Reimbursement\"}" | jq -r '.message'

echo -e "\n========================================="
echo "📊 Итоговые балансы:"
echo "========================================="

echo -e "\n💳 Alice (USD):"
curl -s "$BASE_URL/api/v1/wallets/$WALLET1_ID" \
  -H "Authorization: Bearer $TOKEN1" | jq '.data'

echo -e "\n💳 Bob (EUR):"
curl -s "$BASE_URL/api/v1/wallets/$WALLET2_ID" \
  -H "Authorization: Bearer $TOKEN2" | jq '.data'

echo -e "\n💳 Charlie (GBP):"
curl -s "$BASE_URL/api/v1/wallets/$WALLET3_ID" \
  -H "Authorization: Bearer $TOKEN3" | jq '.data'

echo -e "\n========================================="
echo "✅ Готово!"
echo "========================================="
