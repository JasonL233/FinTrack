#!/bin/bash

# FinTrack Services - Startup Script
# Automatically loads .env and starts all services

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "🚀 FinTrack Services Startup"
echo "============================"
echo ""

# Load environment variables from .env file
if [ -f .env ]; then
    set -a
    source .env
    set +a
    echo "✅ Loaded environment variables from .env"
else
    echo "⚠️  .env file not found - using defaults"
fi

# Set defaults if not provided
export DB_USER=${DB_USER:-postgres}
export DB_PASSWORD=${DB_PASSWORD:-postgres}
export DB_HOST=${DB_HOST:-localhost}
export DB_USER_PORT=${DB_USER_PORT:-5432}
export DB_TRANSACTION_PORT=${DB_TRANSACTION_PORT:-5433}

# Verify JWT_SECRET is set
if [ -z "$JWT_SECRET" ]; then
    echo "⚠️  WARNING: JWT_SECRET is not set!"
    echo "   Setting a default value (NOT SECURE for production!)"
    export JWT_SECRET=${JWT_SECRET:-default-secret-key-change-in-production-at-least-64-characters-long-for-hmac-sha512}
    echo "   To set properly, create a .env file with JWT_SECRET"
else
    echo "✅ JWT_SECRET is set (length: ${#JWT_SECRET} characters)"
fi

# Set JWT_EXPIRATION if not set
export JWT_EXPIRATION=${JWT_EXPIRATION:-86400000}

echo ""
echo "📋 Configuration:"
echo "   DB_USER: $DB_USER"
echo "   DB_HOST: $DB_HOST"
echo "   DB_USER_PORT: ${DB_USER_PORT}"
echo "   DB_TRANSACTION_PORT: ${DB_TRANSACTION_PORT}"
echo ""

# Check what to start
if [ "$1" = "all" ] || [ "$1" = "" ]; then
    # Start all services in background
    echo "🚀 Starting all services in background..."
    echo ""
    
    echo "Starting user-service (port 8081)..."
    (cd "$SCRIPT_DIR/user-service" && mvn spring-boot:run > "$SCRIPT_DIR/logs/user-service.log" 2>&1) &
    USER_PID=$!
    echo "  ✓ User service started (PID: $USER_PID)"
    
    sleep 2
    
    echo "Starting transaction-service (port 8082)..."
    (cd "$SCRIPT_DIR/transaction-service" && mvn spring-boot:run > "$SCRIPT_DIR/logs/transaction-service.log" 2>&1) &
    TRANSACTION_PID=$!
    echo "  ✓ Transaction service started (PID: $TRANSACTION_PID)"
    
    sleep 2
    
    echo "Starting api-gateway (port 8080)..."
    (cd "$SCRIPT_DIR/api-gateway" && mvn spring-boot:run > "$SCRIPT_DIR/logs/api-gateway.log" 2>&1) &
    GATEWAY_PID=$!
    echo "  ✓ API gateway started (PID: $GATEWAY_PID)"
    
    sleep 2
    
    echo "Starting notification-service (port 8083)..."
    (cd "$SCRIPT_DIR/notification-service" && mvn spring-boot:run > "$SCRIPT_DIR/logs/notification-service.log" 2>&1) &
    NOTIFICATION_PID=$!
    echo "  ✓ Notification service started (PID: $NOTIFICATION_PID)"
    
    echo ""
    echo "✅ All services started!"
    echo ""
    echo "📋 Service PIDs:"
    echo "  User Service:        $USER_PID"
    echo "  Transaction Service: $TRANSACTION_PID"
    echo "  API Gateway:         $GATEWAY_PID"
    echo "  Notification Service: $NOTIFICATION_PID"
    echo ""
    echo "📝 Logs are in the 'logs' directory:"
    echo "  tail -f logs/user-service.log"
    echo "  tail -f logs/transaction-service.log"
    echo "  tail -f logs/api-gateway.log"
    echo "  tail -f logs/notification-service.log"
    echo ""
    echo "🛑 To stop all services:"
    echo "  kill $USER_PID $TRANSACTION_PID $GATEWAY_PID $NOTIFICATION_PID"
    echo "  or: ./start-services.sh stop"
    echo ""
    
    # Save PIDs to file for easy stopping
    mkdir -p "$SCRIPT_DIR/logs"
    echo "$USER_PID $TRANSACTION_PID $GATEWAY_PID $NOTIFICATION_PID" > "$SCRIPT_DIR/logs/service.pids"
    
elif [ "$1" = "stop" ]; then
    # Stop all services
    if [ -f "$SCRIPT_DIR/logs/service.pids" ]; then
        echo "🛑 Stopping all services..."
        kill $(cat "$SCRIPT_DIR/logs/service.pids") 2>/dev/null
        rm "$SCRIPT_DIR/logs/service.pids"
        echo "✅ All services stopped"
    else
        echo "⚠️  No running services found (logs/service.pids not found)"
    fi
    
elif [ "$1" != "" ]; then
    # Start a specific service
    SERVICE=$1
    echo "Starting $SERVICE..."
    echo ""
    
    case $SERVICE in
        user-service)
            cd "$SCRIPT_DIR/user-service" && mvn spring-boot:run
            ;;
        transaction-service)
            cd "$SCRIPT_DIR/transaction-service" && mvn spring-boot:run
            ;;
        api-gateway)
            cd "$SCRIPT_DIR/api-gateway" && mvn spring-boot:run
            ;;
        notification-service)
            cd "$SCRIPT_DIR/notification-service" && mvn spring-boot:run
            ;;
        *)
            echo "❌ Unknown service: $SERVICE"
            echo ""
            echo "Usage: ./start-services.sh [all|stop|<service-name>]"
            echo ""
            echo "Options:"
            echo "  (no args) or 'all' - Start all services in background"
            echo "  stop               - Stop all running services"
            echo "  user-service       - Start only user service (port 8081)"
            echo "  transaction-service - Start only transaction service (port 8082)"
            echo "  api-gateway        - Start only API gateway (port 8080)"
            echo "  notification-service - Start only notification service (port 8083)"
            exit 1
            ;;
    esac
fi