#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================="
echo "💰 PlastWallet - Test Data Setup"
echo "========================================="

# Создаём пользователя 1
echo -e "\n📝 Creating User 1 (Alice)..."
RESP1=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Johnson","email":"alice@plastwallet.com","password":"password123"}')
echo "$RESP1" | jq -r '.message'
TOKEN1=$(echo $RESP1 | jq -r '.data.token')

# Создаём кошелёк 1 (USD)
echo -e "\n💳 Creating Wallet 1 (USD)..."
WALLET1=$(curl -s -X POST "$BASE_URL/api/v1/wallets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d '{"userId":1,"currency":"USD"}')
echo "$WALLET1" | jq -r '.message'
WALLET1_ID=$(echo $WALLET1 | jq -r '.data.id')
echo "Wallet 1 ID: $WALLET1_ID"

# Создаём пользователя 2
echo -e "\n📝 Creating User 2 (Bob)..."
RESP2=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Bob Smith","email":"bob@plastwallet.com","password":"password123"}')
echo "$RESP2" | jq -r '.message'
TOKEN2=$(echo $RESP2 | jq -r '.data.token')

# Создаём кошелёк 2 (EUR)
echo -e "\n💳 Creating Wallet 2 (EUR)..."
WALLET2=$(curl -s -X POST "$BASE_URL/api/v1/wallets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d '{"userId":2,"currency":"EUR"}')
echo "$WALLET2" | jq -r '.message'
WALLET2_ID=$(echo $WALLET2 | jq -r '.data.id')
echo "Wallet 2 ID: $WALLET2_ID"

# Создаём пользователя 3
echo -e "\n📝 Creating User 3 (Charlie)..."
RESP3=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Charlie Brown","email":"charlie@plastwallet.com","password":"password123"}')
echo "$RESP3" | jq -r '.message'

# Создаём кошелёк 3 (GBP)
echo -e "\n💳 Creating Wallet 3 (GBP)..."
WALLET3=$(curl -s -X POST "$BASE_URL/api/v1/wallets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d '{"userId":3,"currency":"GBP"}')
echo "$WALLET3" | jq -r '.message'
WALLET3_ID=$(echo $WALLET3 | jq -r '.data.id')
echo "Wallet 3 ID: $WALLET3_ID"

echo -e "\n========================================="
echo "💵 Depositing $100 to each wallet..."
echo "========================================="

# Депозит на кошелёк 1
echo -e "\n💰 Depositing \$100 to Wallet 1..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET1_ID/deposit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d '{"amount":100,"description":"Initial deposit"}' | jq -r '.message'

# Депозит на кошелёк 2
echo -e "\n💰 Depositing €100 to Wallet 2..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET2_ID/deposit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d '{"amount":100,"description":"Initial deposit"}' | jq -r '.message'

# Депозит на кошелёк 3
echo -e "\n💰 Depositing £100 to Wallet 3..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET3_ID/deposit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d '{"amount":100,"description":"Initial deposit"}' | jq -r '.message'

echo -e "\n========================================="
echo "🔄 Transferring $50 between wallets..."
echo "========================================="

# Перевод с кошелька 1 на кошелёк 2
echo -e "\n🔄 Transferring \$50 from Wallet 1 → Wallet 2..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET1_ID/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d "{\"toWalletId\":\"$WALLET2_ID\",\"amount\":50,\"description\":\"Payment for services\"}" | jq -r '.message'

# Перевод с кошелька 2 на кошелёк 3
echo -e "\n🔄 Transferring €50 from Wallet 2 → Wallet 3..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET2_ID/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d "{\"toWalletId\":\"$WALLET3_ID\",\"amount\":50,\"description\":\"Refund\"}" | jq -r '.message'

# Перевод с кошелька 3 на кошелёк 1
echo -e "\n🔄 Transferring £50 from Wallet 3 → Wallet 1..."
curl -s -X POST "$BASE_URL/api/v1/wallets/$WALLET3_ID/transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d "{\"toWalletId\":\"$WALLET1_ID\",\"amount\":50,\"description\":\"Reimbursement\"}" | jq -r '.message'

echo -e "\n========================================="
echo "📊 Final Balances:"
echo "========================================="

echo -e "\nWallet 1 (USD):"
curl -s "$BASE_URL/api/v1/wallets/$WALLET1_ID" \
  -H "Authorization: Bearer $TOKEN1" | jq '.data'

echo -e "\nWallet 2 (EUR):"
curl -s "$BASE_URL/api/v1/wallets/$WALLET2_ID" \
  -H "Authorization: Bearer $TOKEN2" | jq '.data'

echo -e "\nWallet 3 (GBP):"
curl -s "$BASE_URL/api/v1/wallets/$WALLET3_ID" \
  -H "Authorization: Bearer $TOKEN2" | jq '.data'

echo -e "\n========================================="
echo "✅ Setup Complete!"
echo "========================================="
