# User Service - Testing and Running Guide

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for PostgreSQL database)
- Postman or curl (for API testing)

## Step 1: Start PostgreSQL Database

Start the PostgreSQL database using Docker Compose:

```bash
# From the project root directory
docker-compose up -d
```

This will start PostgreSQL on port 5432 with:

- Database: `fintrack_users`
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

Or build just the user-service:

```bash
cd user-service
mvn clean install
```

## Step 3: Run the User Service

From the project root:

```bash
cd user-service
mvn spring-boot:run
```

Or run the JAR file:

```bash
cd user-service
java -jar target/user-service-1.0.0.jar
```

The service will start on **http://localhost:8081**

## Step 4: Test the API

### Health Check

```bash
curl http://localhost:8081/api/users/health
```

Expected response: `User Service is running!`

### Register a New User

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

Expected response:

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**Save the token from the response** - you'll need it for protected endpoints.

### Login

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

Expected response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

### Get User Profile (Protected Endpoint)

Replace `YOUR_TOKEN_HERE` with the token from login/register:

```bash
curl -X GET http://localhost:8081/api/users/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

Expected response:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "role": "USER",
    "active": true,
    "createdAt": "2024-01-01T12:00:00"
  }
}
```

## Testing with Postman

1. Import the Postman collection (if available)
2. Set the base URL: `http://localhost:8081`
3. For protected endpoints, add the token in the Authorization header:
   - Type: Bearer Token
   - Token: (paste your JWT token)

## Common Issues

### Database Connection Error

- Ensure PostgreSQL is running: `docker ps`
- Check database credentials in `application.yml`
- Verify port 5432 is not in use by another service

### Port Already in Use

- Change the port in `application.yml`: `server.port: 8082`
- Or stop the service using port 8081

### JWT Token Expired

- Tokens expire after 24 hours (86400000 ms)
- Re-login to get a new token

### Validation Errors

- Email must be valid format
- Password must be at least 8 characters
- All required fields must be provided

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
