# Transaction Service - Testing and Running Guide

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for PostgreSQL database)
- Postman or curl (for API testing)
- User Service running (for JWT token authentication)

## Step 1: Start PostgreSQL Database

Start the PostgreSQL database using Docker Compose:

```bash
# From the project root directory
docker-compose up -d
```

This will start PostgreSQL on port 5433 with:

- Database: `fintrack_transactions`
- Username: `postgres`
- Password: `postgres`

Verify the database is running:

```bash
docker ps
```

## Step 2: Build the Project

From the project root, build all modules:

```bash
mvn clean install
```

Or build just the transaction-service:

```bash
cd transaction-service
mvn clean install
```

## Step 3: Set Environment Variables

Create a `.env` file in the project root (if not already exists) with:

```bash
JWT_SECRET=your-secret-key-here-make-it-long-and-secure-at-least-256-bits
JWT_EXPIRATION=86400000
DB_HOST=localhost
DB_USER=postgres
DB_PASSWORD=postgres
```

**Important**: The `JWT_SECRET` must match the one used in the user-service for token validation to work.

## Step 4: Run the Transaction Service

From the project root:

```bash
cd transaction-service
mvn spring-boot:run
```

Or run the JAR file:

```bash
cd transaction-service
java -jar target/transaction-service-1.0.0.jar
```

The service will start on **http://localhost:8082**

## Step 5: Get JWT Token from User Service

Before testing transaction endpoints, you need a JWT token from the user-service. Make sure user-service is running on port 8081.

### Register/Login to Get Token

```bash
# Register a new user (or login if already exists)
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1234567890"
  }'
```

**Save the token from the response** - you'll need it for all transaction endpoints.

Or login:

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "password123"
  }'
```

## Step 6: Test the Transaction Service API

Replace `YOUR_TOKEN_HERE` with the token from Step 5 in all examples below.

### Health Check (No Authentication Required)

```bash
curl http://localhost:8082/api/transactions/health
```

Expected response: `Transaction Service is running!`

### Create a Transaction

**Income Transaction:**

```bash
curl -X POST http://localhost:8082/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInN1YiI6InRlc3QudXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTc2MzE3NDQ4NiwiZXhwIjoxNzYzMjYwODg2fQ.ZXUlq_oH06p_z5qe-4dsBfvhQr6gKa1ikpxGbQy7cBcT8qcyO_WnX75KN_ZgssRTIrplpX8bn-nivZOtFlsJeQ" \
  -d '{
    "amount": 5000.00,
    "type": "INCOME",
    "category": "SALARY",
    "description": "Monthly salary",
    "transactionDate": "2024-01-15",
    "merchant": "Company Inc",
    "notes": "January 2024 salary"
  }'
```

**Expense Transaction:**

```bash
curl -X POST http://localhost:8082/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInN1YiI6InRlc3QudXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTc2MzE3NDQ4NiwiZXhwIjoxNzYzMjYwODg2fQ.ZXUlq_oH06p_z5qe-4dsBfvhQr6gKa1ikpxGbQy7cBcT8qcyO_WnX75KN_ZgssRTIrplpX8bn-nivZOtFlsJeQ" \
  -d '{
    "amount": 150.50,
    "type": "EXPENSE",
    "category": "GROCERIES",
    "description": "Weekly grocery shopping",
    "transactionDate": "2024-01-16",
    "merchant": "Supermarket",
    "accountNumber": "****1234",
    "notes": "Weekly groceries"
  }'
```

Expected response:

```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "amount": 150.5,
    "type": "EXPENSE",
    "category": "GROCERIES",
    "description": "Weekly grocery shopping",
    "transactionDate": "2024-01-16",
    "merchant": "Supermarket",
    "accountNumber": "****1234",
    "notes": "Weekly groceries",
    "status": "COMPLETED",
    "referenceNumber": "TXN-ABC12345",
    "createdAt": "2024-01-16T10:30:00",
    "updatedAt": "2024-01-16T10:30:00"
  }
}
```

**Save the transaction ID** from the response for subsequent operations.

### Get Transaction by ID

```bash
curl -X GET http://localhost:8082/api/transactions/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get All Transactions (Paginated)

```bash
# Get first page (10 transactions)
curl -X GET "http://localhost:8082/api/transactions?page=0&size=10&sortBy=transactionDate&sortDir=DESC" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Transactions by Type

```bash
# Get all INCOME transactions
curl -X GET "http://localhost:8082/api/transactions/type/INCOME?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Get all EXPENSE transactions
curl -X GET "http://localhost:8082/api/transactions/type/EXPENSE?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Transactions by Category

```bash
# Get all GROCERIES transactions
curl -X GET "http://localhost:8082/api/transactions/category/GROCERIES?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Transactions by Date Range

```bash
curl -X GET "http://localhost:8082/api/transactions/date-range?startDate=2024-01-01&endDate=2024-01-31&page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Update a Transaction

```bash
curl -X PUT http://localhost:8082/api/transactions/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "amount": 200.00,
    "description": "Updated description",
    "notes": "Updated notes"
  }'
```

### Delete a Transaction

```bash
curl -X DELETE http://localhost:8082/api/transactions/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Transaction Summary

```bash
curl -X GET http://localhost:8082/api/transactions/summary \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

Expected response:

```json
{
  "success": true,
  "data": {
    "totalIncome": 5000.0,
    "totalExpense": 150.5,
    "netBalance": 4849.5,
    "totalTransactions": 2,
    "spendingByCategory": {
      "GROCERIES": 150.5
    }
  }
}
```

## Transaction Types

- `INCOME` - Money received
- `EXPENSE` - Money spent

## Transaction Categories

### Income Categories:

- `SALARY`
- `FREELANCE`
- `INVESTMENT`
- `BUSINESS`
- `GIFT`
- `REFUND`
- `OTHER_INCOME`

### Expense Categories:

- `FOOD_DINING`
- `GROCERIES`
- `SHOPPING`
- `ENTERTAINMENT`
- `TRANSPORTATION`
- `UTILITIES`
- `HEALTHCARE`
- `INSURANCE`
- `EDUCATION`
- `TRAVEL`
- `RENT`
- `MORTGAGE`
- `PERSONAL_CARE`
- `SUBSCRIPTION`
- `CHARITY`
- `OTHER_EXPENSE`

## Testing with Postman

1. Import the Postman collection (if available)
2. Set the base URL: `http://localhost:8082`
3. For protected endpoints, add the token in the Authorization header:
   - Type: Bearer Token
   - Token: (paste your JWT token from user-service)

## Using the Test Script

A bash script is provided for automated testing:

```bash
cd transaction-service
chmod +x test-api.sh
./test-api.sh
```

**Note**: Make sure you have `jq` installed for JSON parsing:

```bash
# macOS
brew install jq

# Linux
sudo apt-get install jq
```

## Common Issues

### Database Connection Error

- Ensure PostgreSQL is running: `docker ps`
- Check database credentials in `application.yml`
- Verify port 5433 is not in use by another service
- Check that `DB_HOST` environment variable is set correctly

### JWT Authentication Error

- Ensure user-service is running on port 8081
- Verify `JWT_SECRET` matches between user-service and transaction-service
- Check that token is not expired (tokens expire after 24 hours)
- Ensure token is included in Authorization header: `Bearer <token>`

### Port Already in Use

- Change the port in `application.yml`: `server.port: 8083`
- Or stop the service using port 8082

### Validation Errors

- Amount must be greater than 0.01
- Transaction type must be `INCOME` or `EXPENSE`
- Category must match the transaction type (e.g., `SALARY` for `INCOME`, `GROCERIES` for `EXPENSE`)
- Description is required and cannot exceed 500 characters
- Transaction date is required

### Transaction Not Found

- Verify the transaction ID exists
- Ensure the transaction belongs to the authenticated user
- Transactions are user-specific and cannot be accessed by other users

## Stopping Services

Stop the application: `Ctrl+C` in the terminal

Stop PostgreSQL:

```bash
docker-compose down
```

Stop and remove volumes (clears database):

```bash
docker-compose down -v
```

## Complete Testing Workflow

1. Start databases: `docker-compose up -d`
2. Start user-service: `cd user-service && mvn spring-boot:run`
3. Get JWT token: Register/login via user-service
4. Start transaction-service: `cd transaction-service && mvn spring-boot:run`
5. Test endpoints using curl or Postman with the JWT token

## Example Complete Test Sequence

```bash
# 1. Get token from user-service
TOKEN=$(curl -s -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  | jq -r '.data.token')

# 2. Create income transaction
curl -X POST http://localhost:8082/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 5000.00,
    "type": "INCOME",
    "category": "SALARY",
    "description": "Monthly salary",
    "transactionDate": "2024-01-15"
  }'

# 3. Create expense transaction
curl -X POST http://localhost:8082/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "amount": 150.50,
    "type": "EXPENSE",
    "category": "GROCERIES",
    "description": "Weekly groceries",
    "transactionDate": "2024-01-16"
  }'

# 4. Get summary
curl -X GET http://localhost:8082/api/transactions/summary \
  -H "Authorization: Bearer $TOKEN"
```

# View all transactions in database
docker exec fintrack-postgres-transactions psql -U jlin817 -d fintrack_transactions -c "SELECT * FROM transactions ORDER BY created_at DESC;"

