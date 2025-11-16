# 📋 Transaction Service - API & Database Summary

## 🗄️ Database Schema

### PostgreSQL Database: `fintrack_transactions`

#### Table: `transactions`

| Column            | Type         | Constraints                 | Description                        |
| ----------------- | ------------ | --------------------------- | ---------------------------------- |
| `id`              | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique transaction ID               |
| `user_id`         | BIGINT       | NOT NULL                    | Foreign key to user (from user-service) |
| `amount`          | NUMERIC(38,2)| NOT NULL                    | Transaction amount                 |
| `type`            | VARCHAR      | NOT NULL                    | Transaction type (INCOME, EXPENSE) |
| `category`        | VARCHAR      | NOT NULL                    | Transaction category (see below)   |
| `description`     | VARCHAR(500) | NOT NULL                    | Transaction description             |
| `transaction_date`| DATE         | NOT NULL                    | Date when transaction occurred      |
| `merchant`        | VARCHAR(100) | NULLABLE                    | Merchant/vendor name                |
| `account_number`  | VARCHAR(50)  | NULLABLE                    | Account number used                 |
| `notes`           | VARCHAR(1000)| NULLABLE                   | Additional notes                    |
| `status`          | VARCHAR      | NOT NULL, DEFAULT 'COMPLETED' | Transaction status (see below)   |
| `reference_number`| VARCHAR(100) | NULLABLE                    | External reference number           |
| `created_at`      | TIMESTAMP    | NOT NULL, AUTO              | Transaction creation time           |
| `updated_at`      | TIMESTAMP    | NOT NULL, AUTO              | Last update time                    |

**Indexes:**
- `idx_user_id` - For faster user-based queries
- `idx_transaction_date` - For faster date-based queries
- `idx_category` - For faster category-based queries

**Example Record:**

```sql
id: 1
user_id: 1
amount: 5000.00
type: "INCOME"
category: "SALARY"
description: "Monthly salary"
transaction_date: "2024-01-15"
merchant: "Company Inc"
account_number: null
notes: "January 2024 salary"
status: "COMPLETED"
reference_number: "TXN-D3D6D847"
created_at: "2025-11-14 18:50:47.542244"
updated_at: "2025-11-14 18:50:47.542336"
```

### Enums

#### TransactionType
- `INCOME` - Money received
- `EXPENSE` - Money spent

#### TransactionCategory

**Income Categories:**
- `SALARY` - Regular salary/wages
- `FREELANCE` - Freelance income
- `INVESTMENT` - Investment returns
- `BUSINESS` - Business income
- `GIFT` - Gifts received
- `REFUND` - Refunds received
- `OTHER_INCOME` - Other income sources

**Expense Categories:**
- `FOOD_DINING` - Restaurants, takeout
- `GROCERIES` - Grocery shopping
- `SHOPPING` - General shopping
- `ENTERTAINMENT` - Movies, games, etc.
- `TRANSPORTATION` - Gas, public transit, etc.
- `UTILITIES` - Electricity, water, internet
- `HEALTHCARE` - Medical expenses
- `INSURANCE` - Insurance payments
- `EDUCATION` - Tuition, books, courses
- `TRAVEL` - Travel expenses
- `RENT` - Rent payments
- `MORTGAGE` - Mortgage payments
- `PERSONAL_CARE` - Personal grooming
- `SUBSCRIPTION` - Subscriptions (Netflix, etc.)
- `CHARITY` - Charitable donations
- `OTHER_EXPENSE` - Other expenses

#### TransactionStatus
- `PENDING` - Transaction pending
- `COMPLETED` - Transaction completed
- `CANCELLED` - Transaction cancelled
- `FAILED` - Transaction failed

---

## 🌐 API Endpoints

### Base URL: `http://localhost:8082/api/transactions`

### 1. Health Check

**GET** `/health`

**Description:** Check if service is running

**Authentication:** None (Public)

**Response:**

```
Transaction Service is running!
```

---

### 2. Create Transaction

**POST** `/`

**Description:** Create a new transaction

**Authentication:** Required (JWT Token)

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json
```

**Request Body:**

```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "SALARY",
  "description": "Monthly salary",
  "transactionDate": "2024-01-15",
  "merchant": "Company Inc",
  "accountNumber": "****1234",
  "notes": "January 2024 salary"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "amount": 5000.00,
    "type": "INCOME",
    "category": "SALARY",
    "description": "Monthly salary",
    "transactionDate": "2024-01-15",
    "merchant": "Company Inc",
    "accountNumber": "****1234",
    "notes": "January 2024 salary",
    "status": "COMPLETED",
    "referenceNumber": "TXN-D3D6D847",
    "createdAt": "2025-11-14T18:50:47.542244",
    "updatedAt": "2025-11-14T18:50:47.542336"
  },
  "timestamp": "2025-11-14T18:50:47.574382"
}
```

**What Happens:**

1. Extracts userId from JWT token
2. Validates request data
3. Generates unique reference number
4. Sets default status to COMPLETED
5. Saves transaction to database
6. Returns created transaction

---

### 3. Get Transaction by ID

**GET** `/{id}`

**Description:** Get a specific transaction by ID

**Authentication:** Required (JWT Token)

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "amount": 5000.00,
    "type": "INCOME",
    "category": "SALARY",
    "description": "Monthly salary",
    "transactionDate": "2024-01-15",
    "merchant": "Company Inc",
    "accountNumber": "****1234",
    "notes": "January 2024 salary",
    "status": "COMPLETED",
    "referenceNumber": "TXN-D3D6D847",
    "createdAt": "2025-11-14T18:50:47.542244",
    "updatedAt": "2025-11-14T18:50:47.542336"
  },
  "timestamp": "2025-11-14T18:50:47.574382"
}
```

**What Happens:**

1. Extracts userId from JWT token
2. Verifies transaction belongs to user
3. Retrieves transaction from database
4. Returns transaction data

---

### 4. Get All Transactions

**GET** `/?page=0&size=10&sortBy=transactionDate&sortDir=DESC`

**Description:** Get paginated list of all user's transactions

**Authentication:** Required (JWT Token)

**Query Parameters:**

- `page` (default: 0) - Page number (0-indexed)
- `size` (default: 10) - Number of items per page
- `sortBy` (default: "transactionDate") - Field to sort by
- `sortDir` (default: "DESC") - Sort direction (ASC/DESC)

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 1,
        "amount": 5000.00,
        "type": "INCOME",
        "category": "SALARY",
        "description": "Monthly salary",
        "transactionDate": "2024-01-15",
        "status": "COMPLETED",
        "createdAt": "2025-11-14T18:50:47.542244"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "first": true,
    "numberOfElements": 1
  },
  "timestamp": "2025-11-14T18:50:47.574382"
}
```

---

### 5. Get Transactions by Type

**GET** `/type/{type}?page=0&size=10&sortBy=transactionDate&sortDir=DESC`

**Description:** Get transactions filtered by type (INCOME or EXPENSE)

**Authentication:** Required (JWT Token)

**Path Parameters:**

- `type` - Transaction type (INCOME or EXPENSE)

**Query Parameters:**

- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `sortBy` (default: "transactionDate") - Sort field
- `sortDir` (default: "DESC") - Sort direction

**Example:**

```
GET /api/transactions/type/INCOME?page=0&size=10
```

**Response:** Same format as "Get All Transactions"

---

### 6. Get Transactions by Category

**GET** `/category/{category}?page=0&size=10&sortBy=transactionDate&sortDir=DESC`

**Description:** Get transactions filtered by category

**Authentication:** Required (JWT Token)

**Path Parameters:**

- `category` - Transaction category (e.g., GROCERIES, SALARY)

**Query Parameters:**

- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `sortBy` (default: "transactionDate") - Sort field
- `sortDir` (default: "DESC") - Sort direction

**Example:**

```
GET /api/transactions/category/GROCERIES?page=0&size=10
```

**Response:** Same format as "Get All Transactions"

---

### 7. Get Transactions by Date Range

**GET** `/date-range?startDate=2024-01-01&endDate=2024-01-31&page=0&size=10`

**Description:** Get transactions within a date range

**Authentication:** Required (JWT Token)

**Query Parameters:**

- `startDate` (required) - Start date (ISO format: YYYY-MM-DD)
- `endDate` (required) - End date (ISO format: YYYY-MM-DD)
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `sortBy` (default: "transactionDate") - Sort field
- `sortDir` (default: "DESC") - Sort direction

**Example:**

```
GET /api/transactions/date-range?startDate=2024-01-01&endDate=2024-01-31
```

**Response:** Same format as "Get All Transactions"

---

### 8. Update Transaction

**PUT** `/{id}`

**Description:** Update an existing transaction

**Authentication:** Required (JWT Token)

**Path Parameters:**

- `id` - Transaction ID

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json
```

**Request Body (all fields optional):**

```json
{
  "amount": 200.00,
  "description": "Updated description",
  "merchant": "Updated merchant",
  "notes": "Updated notes",
  "status": "PENDING"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Transaction updated successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "amount": 200.00,
    "type": "EXPENSE",
    "category": "GROCERIES",
    "description": "Updated description",
    "transactionDate": "2024-01-15",
    "merchant": "Updated merchant",
    "notes": "Updated notes",
    "status": "PENDING",
    "referenceNumber": "TXN-D3D6D847",
    "createdAt": "2025-11-14T18:50:47.542244",
    "updatedAt": "2025-11-14T18:56:32.525940"
  },
  "timestamp": "2025-11-14T18:56:32.525940"
}
```

**What Happens:**

1. Extracts userId from JWT token
2. Verifies transaction belongs to user
3. Updates only provided fields
4. Updates `updatedAt` timestamp
5. Returns updated transaction

---

### 9. Delete Transaction

**DELETE** `/{id}`

**Description:** Delete a transaction

**Authentication:** Required (JWT Token)

**Path Parameters:**

- `id` - Transaction ID

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Transaction deleted successfully",
  "timestamp": "2025-11-14T18:56:32.674014"
}
```

**What Happens:**

1. Extracts userId from JWT token
2. Verifies transaction belongs to user
3. Deletes transaction from database
4. Returns success message

---

### 10. Get Transaction Summary

**GET** `/summary`

**Description:** Get financial summary for the user

**Authentication:** Required (JWT Token)

**Headers:**

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "totalIncome": 10000.00,
    "totalExpense": 350.50,
    "netBalance": 9649.50,
    "totalTransactions": 4,
    "spendingByCategory": {
      "GROCERIES": 150.50,
      "FOOD_DINING": 200.00
    }
  },
  "timestamp": "2025-11-14T18:56:32.631829"
}
```

**What Happens:**

1. Extracts userId from JWT token
2. Calculates total income (sum of all INCOME transactions)
3. Calculates total expense (sum of all EXPENSE transactions)
4. Calculates net balance (income - expense)
5. Groups expenses by category
6. Returns summary data

---

## 🔐 Authentication Flow

### JWT Token Validation

The transaction service validates JWT tokens issued by the user service:

**Token Requirements:**
- Must be issued by user-service with matching `JWT_SECRET`
- Must contain `userId` claim
- Must contain `sub` (email) claim
- Must not be expired

### How Authentication Works

```
1. Client sends request with JWT token
   Authorization: Bearer <token>
   ↓
2. JwtAuthenticationFilter intercepts request
   ↓
3. Extracts token from Authorization header
   ↓
4. Validates token signature and expiration
   ↓
5. Extracts userId and email from token
   ↓
6. Sets UserContext in request attribute
   ↓
7. Sets Authentication in Spring Security context
   ↓
8. Request proceeds to controller
   ↓
9. Controller extracts userId from UserContext
   ↓
10. Service performs operation with userId
```

---

## 🔄 Complete Flow Diagrams

### Create Transaction Flow

```
Client
  │
  ├─ POST /api/transactions
  │  Header: Authorization: Bearer <token>
  │  Body: { amount, type, category, description, ... }
  │
  ↓
JwtAuthenticationFilter
  │
  ├─ Validate JWT token
  │
  ├─ Extract userId from token
  │
  └─ Set UserContext in request
     ↓
Controller (TransactionController)
  │
  ├─ Extract userId from UserContext
  │
  ├─ Validate request data
  │
  ↓
Service (TransactionService)
  │
  ├─ Generate reference number
  │
  ├─ Set default status (COMPLETED)
  │
  ├─ Save to Database
  │     ↓
  │  PostgreSQL (transactions table)
  │
  └─ Return Response
       { id, userId, amount, type, ... }
```

### Get Transactions Flow

```
Client
  │
  ├─ GET /api/transactions?page=0&size=10
  │  Header: Authorization: Bearer <token>
  │
  ↓
JwtAuthenticationFilter
  │
  ├─ Validate JWT token
  │
  └─ Set UserContext
     ↓
Controller (TransactionController)
  │
  ├─ Extract userId from UserContext
  │
  ├─ Create Pageable object
  │
  ↓
Service (TransactionService)
  │
  ├─ Query Database
  │     ↓
  │  Repository (TransactionRepository)
  │     ↓
  │  PostgreSQL (transactions table)
  │     WHERE user_id = <userId>
  │
  └─ Return Paginated Response
       { content: [...], totalPages, totalElements, ... }
```

### Update Transaction Flow

```
Client
  │
  ├─ PUT /api/transactions/{id}
  │  Header: Authorization: Bearer <token>
  │  Body: { amount, description, ... }
  │
  ↓
JwtAuthenticationFilter
  │
  └─ Validate & Set UserContext
     ↓
Controller (TransactionController)
  │
  ├─ Extract userId from UserContext
  │
  ↓
Service (TransactionService)
  │
  ├─ Load transaction from Database
  │
  ├─ Verify transaction belongs to user
  │
  ├─ Update fields (only provided ones)
  │
  ├─ Save to Database
  │     ↓
  │  PostgreSQL (transactions table)
  │
  └─ Return Updated Transaction
```

### Get Summary Flow

```
Client
  │
  ├─ GET /api/transactions/summary
  │  Header: Authorization: Bearer <token>
  │
  ↓
JwtAuthenticationFilter
  │
  └─ Validate & Set UserContext
     ↓
Controller (TransactionController)
  │
  ├─ Extract userId from UserContext
  │
  ↓
Service (TransactionService)
  │
  ├─ Query Database
  │     ├─ SUM(amount) WHERE type = 'INCOME'
  │     ├─ SUM(amount) WHERE type = 'EXPENSE'
  │     └─ GROUP BY category WHERE type = 'EXPENSE'
  │
  ├─ Calculate net balance
  │
  └─ Return Summary
       { totalIncome, totalExpense, netBalance, spendingByCategory }
```

---

## 🏗️ Architecture Components

### Layers

```
┌─────────────────────────────────────┐
│         Controller Layer            │
│    (TransactionController)          │
│    - Handles HTTP requests          │
│    - Extracts userId from JWT       │
│    - Validates input                │
│    - Returns responses              │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Service Layer              │
│    (TransactionService)            │
│    - Business logic                │
│    - Transaction validation        │
│    - Reference number generation   │
│    - Summary calculations          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Repository Layer               │
│    (TransactionRepository)          │
│    - Database operations            │
│    - Custom queries                 │
│    - Pagination support             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Database                    │
│    PostgreSQL (fintrack_transactions)│
│    - transactions table             │
│    - Indexes for performance       │
└─────────────────────────────────────┘
```

### Security Components

```
┌─────────────────────────────────────┐
│    SecurityConfig                   │
│    - Configures Spring Security     │
│    - Defines public/protected paths │
│    - Sets up JWT filter             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    JwtAuthenticationFilter          │
│    - Intercepts all requests        │
│    - Validates JWT tokens           │
│    - Extracts userId from token     │
│    - Sets UserContext               │
│    - Sets SecurityContext           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    JwtUtil                          │
│    - Validates JWT tokens           │
│    - Extracts claims (userId, email)│
│    - Checks token expiration        │
└─────────────────────────────────────┘
```

### DTOs (Data Transfer Objects)

```
┌─────────────────────────────────────┐
│    CreateTransactionRequest         │
│    - Input for creating transaction │
│    - Validation annotations         │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│    UpdateTransactionRequest         │
│    - Input for updating transaction │
│    - All fields optional            │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│    TransactionResponse              │
│    - Output format for transactions │
│    - Includes all transaction data  │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│    TransactionSummaryResponse       │
│    - Financial summary data         │
│    - Totals and category breakdown  │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│    UserContext                      │
│    - Stores userId and email        │
│    - Set by JWT filter              │
└─────────────────────────────────────┘
```

---

## 🔧 Configuration

### Database Configuration

- **Host:** `localhost:5433` (configurable via `DB_HOST`)
- **Database:** `fintrack_transactions`
- **Username:** `postgres` (configurable via `DB_USER`)
- **Password:** `postgres` (configurable via `DB_PASSWORD`)
- **Port:** `5433` (configurable via `DB_TRANSACTION_PORT`)
- **JPA:** Auto-updates schema on startup (`ddl-auto: update`)

### JWT Configuration

- **Secret:** Must match user-service secret (via `JWT_SECRET` env variable)
- **Expiration:** 24 hours (86,400,000 ms) - validated from token
- **Algorithm:** HS512 (HMAC with SHA-512)

### Security Configuration

- **Public Endpoints:** `/health` only
- **Protected Endpoints:** All other endpoints require JWT authentication
- **Session:** Stateless (no HTTP sessions)
- **CSRF:** Disabled (using JWT instead)

### Server Configuration

- **Port:** `8082`
- **Context Path:** `/api/transactions`

---

## 📊 Data Flow Example

### Complete Transaction Journey

```
1. Create Transaction
   Client → POST /api/transactions (with JWT) → JwtAuthenticationFilter
   Filter → Validate JWT → Extract userId → Controller
   Controller → Service → Generate reference → Database
   Database → Service → Controller → Client (transaction created)

2. Get All Transactions
   Client → GET /api/transactions (with JWT) → JwtAuthenticationFilter
   Filter → Validate JWT → Extract userId → Controller
   Controller → Service → Repository → Database (WHERE user_id = ?)
   Database → Repository → Service → Controller → Client (paginated list)

3. Get Summary
   Client → GET /api/transactions/summary (with JWT) → JwtAuthenticationFilter
   Filter → Validate JWT → Extract userId → Controller
   Controller → Service → Repository → Database
   Database → Aggregate queries (SUM, GROUP BY) → Service
   Service → Calculate totals → Controller → Client (summary data)

4. Update Transaction
   Client → PUT /api/transactions/{id} (with JWT) → JwtAuthenticationFilter
   Filter → Validate JWT → Extract userId → Controller
   Controller → Service → Load transaction → Verify ownership
   Service → Update fields → Database → Controller → Client (updated transaction)

5. Delete Transaction
   Client → DELETE /api/transactions/{id} (with JWT) → JwtAuthenticationFilter
   Filter → Validate JWT → Extract userId → Controller
   Controller → Service → Load transaction → Verify ownership
   Service → Delete from Database → Controller → Client (success message)
```

---

## 🎯 Key Features

✅ **Transaction CRUD** - Create, Read, Update, Delete transactions
✅ **JWT Authentication** - Validates tokens from user-service
✅ **User Isolation** - Users can only access their own transactions
✅ **Pagination** - All list endpoints support pagination
✅ **Filtering** - Filter by type, category, and date range
✅ **Sorting** - Sort by any field in ascending/descending order
✅ **Financial Summary** - Calculate income, expenses, and net balance
✅ **Category Breakdown** - Group expenses by category
✅ **Reference Numbers** - Auto-generated unique reference numbers
✅ **Status Management** - Track transaction status (PENDING, COMPLETED, etc.)
✅ **Automatic Timestamps** - createdAt and updatedAt auto-managed
✅ **Input Validation** - Request validation with error messages
✅ **Error Handling** - Global exception handler for consistent error responses
✅ **Database Indexes** - Optimized queries with indexes on user_id, date, category

---

## 🔍 Query Examples

### Get All Income Transactions

```bash
GET /api/transactions/type/INCOME?page=0&size=20&sortBy=amount&sortDir=DESC
Authorization: Bearer <token>
```

### Get Grocery Expenses This Month

```bash
GET /api/transactions/date-range?startDate=2024-01-01&endDate=2024-01-31&page=0&size=10
GET /api/transactions/category/GROCERIES?page=0&size=10
Authorization: Bearer <token>
```

### Get Recent Transactions

```bash
GET /api/transactions?page=0&size=10&sortBy=createdAt&sortDir=DESC
Authorization: Bearer <token>
```

---

## 🚀 Next Steps

- [ ] Add transaction attachments/receipts
- [ ] Add recurring transaction support
- [ ] Add transaction tags/labels
- [ ] Add budget tracking and alerts
- [ ] Add export functionality (CSV, PDF)
- [ ] Add transaction search functionality
- [ ] Add transaction templates
- [ ] Add transaction splitting (split bills)
- [ ] Add transaction reconciliation
- [ ] Add transaction analytics and charts
- [ ] Add transaction import from bank statements
- [ ] Add transaction notifications
- [ ] Add transaction sharing/collaboration
- [ ] Add transaction archiving

---

## 📝 Notes

- All transactions are user-scoped - users can only see/modify their own transactions
- Reference numbers are auto-generated using UUID format (TXN-XXXXXXXX)
- Transaction status defaults to COMPLETED on creation
- Amount is stored as NUMERIC(38,2) for precise decimal handling
- All timestamps are in UTC
- Pagination uses 0-based indexing (first page is page 0)
- Date range queries are inclusive of both start and end dates

