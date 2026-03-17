#!/bin/bash

BASE_URL="http://localhost:8080"
ALICE_UUID="3f672c1a-71c4-424d-b650-eaf9ecbdba89"

echo "Login as Alice..."
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

TOKEN=$(echo "$RESP" | jq -r '.data.token')
echo "Token: ${TOKEN:0:80}..."

echo -e "\n\nGetting wallet by UUID..."
WALLET=$(curl -s -w "\nHTTP Status: %{http_code}" "$BASE_URL/api/v1/wallets/user/$ALICE_UUID" \
  -H "Authorization: Bearer $TOKEN")

echo "$WALLET"
