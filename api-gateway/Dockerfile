# Multi-stage build for smaller image size

# Stage 1: Build
FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent pom and all module poms (Maven needs all modules to resolve parent)
COPY pom.xml .
COPY common/pom.xml common/
COPY user-service/pom.xml user-service/
COPY transaction-service/pom.xml transaction-service/
COPY api-gateway/pom.xml api-gateway/
COPY notification-service/pom.xml notification-service/

# Copy source code for api-gateway
COPY api-gateway/src api-gateway/src

# Build the application
RUN mvn clean package -pl api-gateway -am -DskipTests

# Stage 2: Runtime
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/api-gateway/target/api-gateway-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]