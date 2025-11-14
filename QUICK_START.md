# User Service - Super Quick Start

## Copy-Paste These Commands (In Order)

### 1️⃣ Start Database (10 seconds)

```bash
# From project root
docker-compose up -d
sleep 10
```

### 2️⃣ Build Common Module

```bash
cd common
mvn clean install
```

### 3️⃣ Run User Service

```bash
cd ../user-service
mvn spring-boot:run
```

Wait for: `Started UserServiceApplication in X seconds`

### ✅ Test It (New Terminal)

**Health Check**

```bash
curl http://localhost:8081/api/users/health
# Output: User Service is running!
```

**Register User**

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@test.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "1234567890"
  }'
```

**Copy the token from response!**

**Get Profile**

```bash
curl http://localhost:8081/api/users/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 🛑 Stop Everything

```bash
# Stop service: Ctrl+C in terminal
# Stop database:
docker-compose down
```

### 🐛 Quick Fixes

**Port busy?**

```bash
lsof -i :8081
kill -9 <PID>
```

**Database not running?**

```bash
docker ps
docker-compose up -d
```

**Build failed?**

```bash
mvn clean install -U
```

## 📊 What's Running

```
┌─────────────────┐
│  User Service   │  ← http://localhost:8081
│   Port: 8081    │
└────────┬────────┘
         │
┌────────▼────────┐
│   PostgreSQL    │  ← localhost:5432
│  fintrack_users │
└─────────────────┘
  (Docker Container)
```

## 🎯 Complete Example

```bash
# Terminal 1: Start everything
cd "/Users/lin/J Folder/Personal/FinTrack"

# Start database
docker-compose up -d
sleep 10

# Build common module
cd common && mvn clean install

# Run service
cd ../user-service && mvn spring-boot:run

# Terminal 2: Test
curl http://localhost:8081/api/users/health
# ✅ User Service is running!
```

## ✨ All Working?

- [ ] Database container running
- [ ] Service shows "Started UserServiceApplication"
- [ ] Health check works
- [ ] Can register users
- [ ] Can login
- [ ] Can get profile

All checked? Perfect! 🎊

## 📝 Next Steps

- ✅ Test all API endpoints
- ✅ Try error cases (wrong password, etc.)
- 🎯 Build Transaction Service next!
