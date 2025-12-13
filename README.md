Register:
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "1234567890"
  }'

Login:
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jlin18@gmail.com",
    "password": "password123"
  }'

Create Transaction (Publish to Kafka)
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjcsInN1YiI6ImpsaW4xOEBnbWFpbC5jb20iLCJpYXQiOjE3NjU2MTcxNjMsImV4cCI6MTc2NTcwMzU2M30.VNyyHnnQ9JvVeYQs8ZLZmQEm9s71bHp2NDiPrslN088G48vv3TCvfB7f5nLRtpEXxe8TFFoCdYiZ7aoJddBePg" \
  -d '{
    "amount": 125.50,
    "type": "EXPENSE",
    "category": "GROCERIES",
    "description": "Weekly shopping with Kafka",
    "transactionDate": "2024-12-10",
    "merchant": "Whole Foods"
  }'