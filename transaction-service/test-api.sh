#!/bin/bash

# Transaction Service API Testing Script
# Make sure both user-service and transaction-service are running
# User-service: http://localhost:8081
# Transaction-service: http://localhost:8082

USER_SERVICE_URL="http://localhost:8081/api/users"
TRANSACTION_SERVICE_URL="http://localhost:8082/api/transactions"

echo "=== Testing Transaction Service API ==="
echo ""

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "ERROR: jq is not installed. Please install it first:"
    echo "  macOS: brew install jq"
    echo "  Linux: sudo apt-get install jq"
    exit 1
fi

# Step 1: Health Check - Transaction Service
echo "1. Transaction Service Health Check..."
HEALTH_RESPONSE=$(curl -s -X GET "$TRANSACTION_SERVICE_URL/health")
echo "$HEALTH_RESPONSE"
echo ""

# Step 2: Get JWT Token from User Service
echo "2. Getting JWT token from user-service..."

# Try to login first
LOGIN_RESPONSE=$(curl -s -X POST "$USER_SERVICE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "password123"
  }')

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token // empty')

# If login fails, try to register
if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "Login failed, attempting registration..."
  REGISTER_RESPONSE=$(curl -s -X POST "$USER_SERVICE_URL/register" \
    -H "Content-Type: application/json" \
    -d '{
      "email": "test.user@example.com",
      "password": "password123",
      "firstName": "Test",
      "lastName": "User",
      "phoneNumber": "+1234567890"
    }')
  
  TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.data.token // empty')
fi

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "ERROR: Failed to get JWT token. Make sure user-service is running."
  exit 1
fi

echo "Token received: ${TOKEN:0:50}..."
echo ""

# Step 3: Create Income Transaction
echo "3. Creating INCOME transaction..."
INCOME_RESPONSE=$(curl -s -X POST "$TRANSACTION_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 5000.00,
    "type": "INCOME",
    "category": "SALARY",
    "description": "Monthly salary - Test",
    "transactionDate": "2024-01-15",
    "merchant": "Test Company",
    "notes": "Test transaction"
  }')

echo "$INCOME_RESPONSE" | jq '.'
INCOME_ID=$(echo "$INCOME_RESPONSE" | jq -r '.data.id // empty')
echo ""

# Step 4: Create Expense Transaction
echo "4. Creating EXPENSE transaction..."
EXPENSE_RESPONSE=$(curl -s -X POST "$TRANSACTION_SERVICE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 150.50,
    "type": "EXPENSE",
    "category": "GROCERIES",
    "description": "Weekly groceries - Test",
    "transactionDate": "2024-01-16",
    "merchant": "Test Supermarket",
    "notes": "Test expense transaction"
  }')

echo "$EXPENSE_RESPONSE" | jq '.'
EXPENSE_ID=$(echo "$EXPENSE_RESPONSE" | jq -r '.data.id // empty')
echo ""

# Step 5: Get All Transactions
echo "5. Getting all transactions..."
curl -s -X GET "$TRANSACTION_SERVICE_URL?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# Step 6: Get Transaction by ID (if we have an ID)
if [ -n "$INCOME_ID" ] && [ "$INCOME_ID" != "null" ]; then
  echo "6. Getting transaction by ID: $INCOME_ID..."
  curl -s -X GET "$TRANSACTION_SERVICE_URL/$INCOME_ID" \
    -H "Authorization: Bearer $TOKEN" | jq '.'
  echo ""
fi

# Step 7: Get Transactions by Type
echo "7. Getting INCOME transactions..."
curl -s -X GET "$TRANSACTION_SERVICE_URL/type/INCOME?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

echo "8. Getting EXPENSE transactions..."
curl -s -X GET "$TRANSACTION_SERVICE_URL/type/EXPENSE?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# Step 8: Get Transactions by Category
echo "9. Getting GROCERIES transactions..."
curl -s -X GET "$TRANSACTION_SERVICE_URL/category/GROCERIES?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# Step 9: Get Transactions by Date Range
echo "10. Getting transactions by date range (2024-01-01 to 2024-01-31)..."
curl -s -X GET "$TRANSACTION_SERVICE_URL/date-range?startDate=2024-01-01&endDate=2024-01-31&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# Step 10: Update Transaction (if we have an ID)
if [ -n "$EXPENSE_ID" ] && [ "$EXPENSE_ID" != "null" ]; then
  echo "11. Updating transaction ID: $EXPENSE_ID..."
  UPDATE_RESPONSE=$(curl -s -X PUT "$TRANSACTION_SERVICE_URL/$EXPENSE_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "amount": 200.00,
      "description": "Updated description - Test",
      "notes": "Updated notes"
    }')
  echo "$UPDATE_RESPONSE" | jq '.'
  echo ""
fi

# Step 11: Get Transaction Summary
echo "12. Getting transaction summary..."
curl -s -X GET "$TRANSACTION_SERVICE_URL/summary" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# Step 12: Delete Transaction (if we have an ID)
if [ -n "$EXPENSE_ID" ] && [ "$EXPENSE_ID" != "null" ]; then
  echo "13. Deleting transaction ID: $EXPENSE_ID..."
  DELETE_RESPONSE=$(curl -s -X DELETE "$TRANSACTION_SERVICE_URL/$EXPENSE_ID" \
    -H "Authorization: Bearer $TOKEN")
  echo "$DELETE_RESPONSE" | jq '.'
  echo ""
fi

echo "=== Testing Complete ==="

