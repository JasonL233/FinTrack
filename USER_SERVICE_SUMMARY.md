# 📋 User Service - API & Database Summary

## 🗄️ Database Schema

### PostgreSQL Database: `fintrack_users`

#### Table: `users`

| Column        | Type      | Constraints                 | Description                 |
| ------------- | --------- | --------------------------- | --------------------------- |
| `id`          | BIGINT    | PRIMARY KEY, AUTO_INCREMENT | Unique user ID              |
| `email`       | VARCHAR   | NOT NULL, UNIQUE            | User email address          |
| `password`    | VARCHAR   | NOT NULL                    | Encrypted password (BCrypt) |
| `firstName`   | VARCHAR   | NOT NULL                    | User's first name           |
| `lastName`    | VARCHAR   | NOT NULL                    | User's last name            |
| `phoneNumber` | VARCHAR   | NOT NULL                    | User's phone number         |
| `role`        | VARCHAR   | NOT NULL, DEFAULT 'USER'    | User role (USER, ADMIN)     |
| `active`      | BOOLEAN   | NOT NULL, DEFAULT true      | Account status              |
| `createdAt`   | TIMESTAMP | NOT NULL, AUTO              | Account creation time       |
| `updatedAt`   | TIMESTAMP | NOT NULL, AUTO              | Last update time            |

**Example Record:**

```sql
id: 1
email: "john@example.com"
password: "$2a$10$encrypted..."
firstName: "John"
lastName: "Doe"
phoneNumber: "1234567890"
role: "USER"
active: true
createdAt: "2025-11-13 20:00:00"
updatedAt: "2025-11-13 20:00:00"
```

---

## 🌐 API Endpoints

### Base URL: `http://localhost:8081/api/users`

### 1. Health Check

**GET** `/health`

**Description:** Check if service is running

**Authentication:** None (Public)

**Response:**

```
User Service is running!
```

---

### 2. Register User

**POST** `/register`

**Description:** Create a new user account

**Authentication:** None (Public)

**Request Body:**

```json
{
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "1234567890"
}
```

**Response (201 Created):**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "userId": 1,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "timestamp": "2025-11-13T20:00:00"
}
```

**What Happens:**

1. Validates email format and password (min 8 chars)
2. Checks if email already exists
3. Encrypts password using BCrypt
4. Saves user to database
5. Generates JWT token
6. Returns token and user info

---

### 3. Login

**POST** `/login`

**Description:** Authenticate user and get JWT token

**Authentication:** None (Public)

**Request Body:**

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "userId": 1,
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "timestamp": "2025-11-13T20:00:00"
}
```

**What Happens:**

1. Validates email and password
2. Loads user from database
3. Verifies password using BCrypt
4. Generates new JWT token (24 hour expiration)
5. Returns token and user info

---

### 4. Get Profile

**GET** `/profile`

**Description:** Get current user's profile information

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
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "1234567890",
    "role": "USER",
    "active": true,
    "createdAt": "2025-11-13T20:00:00"
  },
  "timestamp": "2025-11-13T20:00:00"
}
```

**What Happens:**

1. Extracts JWT token from Authorization header
2. Validates token and extracts user email
3. Loads user from database
4. Returns user profile information

---

## 🔐 Authentication Flow

### JWT Token Structure

**Token Payload:**

```json
{
  "userId": 1,
  "sub": "john@example.com",
  "iat": 1733093080,
  "exp": 1733179480
}
```

**Token Expiration:** 24 hours (86,400,000 milliseconds)

**Token Secret:** Configured in `application.yml` (default: `mySecretKeyForJWTTokenGenerationThatIsLongEnoughAndSecure123456789`)

### How Authentication Works

```
1. User registers/logs in
   ↓
2. Service generates JWT token with:
   - userId
   - email (as subject)
   - expiration time
   ↓
3. Token returned to client
   ↓
4. Client stores token (localStorage, cookie, etc.)
   ↓
5. For protected endpoints:
   - Client sends: Authorization: Bearer <token>
   ↓
6. JwtAuthenticationFilter intercepts request
   ↓
7. Validates token signature and expiration
   ↓
8. Extracts user email from token
   ↓
9. Loads user from database
   ↓
10. Sets authentication in Spring Security context
   ↓
11. Request proceeds to controller
```

---

## 🔄 Complete Flow Diagrams

### Registration Flow

```
Client
  │
  ├─ POST /api/users/register
  │  { email, password, firstName, lastName, phoneNumber }
  │
  ↓
Controller (UserController)
  │
  ↓
Service (UserService)
  │
  ├─ Check if email exists
  │
  ├─ Encrypt password (BCrypt)
  │
  ├─ Save to Database
  │     ↓
  │  PostgreSQL (users table)
  │
  ├─ Generate JWT Token
  │
  └─ Return Response
       { token, userId, email, firstName, lastName }
```

### Login Flow

```
Client
  │
  ├─ POST /api/users/login
  │  { email, password }
  │
  ↓
Controller (UserController)
  │
  ↓
Service (UserService)
  │
  ├─ AuthenticationManager.authenticate()
  │     ↓
  │  CustomUserDetailsService
  │     ├─ Load user from DB
  │     └─ Verify password (BCrypt)
  │
  ├─ Generate JWT Token
  │
  └─ Return Response
       { token, userId, email, firstName, lastName }
```

### Protected Endpoint Flow (Get Profile)

```
Client
  │
  ├─ GET /api/users/profile
  │  Header: Authorization: Bearer <token>
  │
  ↓
JwtAuthenticationFilter
  │
  ├─ Extract token from header
  │
  ├─ Validate token (signature, expiration)
  │
  ├─ Extract email from token
  │
  ├─ Load user from database
  │
  └─ Set authentication in SecurityContext
       ↓
Controller (UserController)
  │
  ↓
Service (UserService)
  │
  ├─ Get email from Authentication
  │
  ├─ Load user from Database
  │
  └─ Return Response
       { id, email, firstName, lastName, phoneNumber, role, active, createdAt }
```

---

## 🏗️ Architecture Components

### Layers

```
┌─────────────────────────────────────┐
│         Controller Layer            │
│    (UserController)                 │
│    - Handles HTTP requests          │
│    - Validates input                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Service Layer               │
│    (UserService)                    │
│    - Business logic                 │
│    - Password encryption            │
│    - JWT token generation           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Repository Layer               │
│    (UserRepository)                 │
│    - Database operations            │
│    - CRUD operations                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Database                    │
│    PostgreSQL (fintrack_users)      │
│    - users table                    │
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
│    - Sets authentication context    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    CustomUserDetailsService         │
│    - Loads users from database      │
│    - Converts User to UserDetails   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    JwtUtil                          │
│    - Generates JWT tokens           │
│    - Validates JWT tokens           │
│    - Extracts claims from tokens    │
└─────────────────────────────────────┘
```

---

## 🔧 Configuration

### Database Configuration

- **Host:** `localhost:5432`
- **Database:** `fintrack_users`
- **Username:** `postgres` (configurable via env)
- **Password:** `postgres` (configurable via env)
- **JPA:** Auto-updates schema on startup

### JWT Configuration

- **Secret:** Configurable via `JWT_SECRET` env variable
- **Expiration:** 24 hours (86,400,000 ms)
- **Algorithm:** HS512 (HMAC with SHA-512)

### Security Configuration

- **Public Endpoints:** `/register`, `/login`, `/health`
- **Protected Endpoints:** `/profile` and all others
- **Session:** Stateless (no HTTP sessions)
- **CSRF:** Disabled (using JWT instead)

---

## 📊 Data Flow Example

### Complete User Journey

```
1. User Registration
   Client → POST /register → Controller → Service → Database
   Database → Service → Generate JWT → Controller → Client

2. User Login
   Client → POST /login → Controller → Service → AuthenticationManager
   AuthenticationManager → CustomUserDetailsService → Database
   Database → Verify Password → Generate JWT → Controller → Client

3. Access Protected Resource
   Client → GET /profile (with JWT) → JwtAuthenticationFilter
   Filter → Validate JWT → Extract Email → Load User → Set Auth
   Controller → Service → Database → Controller → Client
```

---

## 🎯 Key Features

✅ **User Registration** - Create new accounts with validation
✅ **User Login** - Authenticate and receive JWT tokens
✅ **Password Security** - BCrypt encryption (one-way hashing)
✅ **JWT Authentication** - Stateless token-based authentication
✅ **Protected Endpoints** - Profile endpoint requires authentication
✅ **Role-Based Access** - User roles (USER, ADMIN) stored in database
✅ **Automatic Timestamps** - createdAt and updatedAt auto-managed
✅ **Input Validation** - Email format, password length, required fields
✅ **Error Handling** - Global exception handler for consistent error responses

---

## 🚀 Next Steps

- [ ] Add email verification
- [ ] Add password reset functionality
- [ ] Add user update endpoint
- [ ] Add user delete endpoint
- [ ] Add admin endpoints
- [ ] Add refresh token mechanism
- [ ] Add rate limiting
- [ ] Add logging and monitoring
