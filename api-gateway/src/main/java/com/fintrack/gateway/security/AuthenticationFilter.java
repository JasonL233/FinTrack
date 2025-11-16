/*
 * Spring Cloud Gateway Filter
 * 1. Sits infront of ALL microservices
 * Validates JWT tokens BEFORE requests reach any service
 * Routes requests to the correct microservice
 * Adds user info to requests for downstream services
 */
package com.fintrack.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    // Public endpoints that don't need authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/users/register",
        "/api/users/login",
        "/api/users/health",
        "/api/transactions/health"
    );

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        // This method called for EVERY HTTP request that goes through the gateway

        return (exchange, chain) -> {   // exchange = Contains request and response, chain = Next filter in the chain
            ServerHttpRequest request = exchange.getRequest();  // Ex) GET http://localhost:8080/api/transaction
            String path = request.getPath().toString();         // Ex) /api/transactions

            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Public endpoint accessed: {}", path);
                return chain.filter(exchange);
            }

            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for : {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Validate Header Format
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for: {}", path);
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate token
                if (!jwtUtil.validateToken(token)) {
                    log.warn("Invalid or expired token for: {}", path);
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user information
                String username = jwtUtil.extractUsername(token);
                Long userId = jwtUtil.extractUserId(token);

                log.debug("Authenticated request: user={}, userId={}, path={}", username, userId, path);

                // Add user info to request headers (userId, email) for downstream services
                // Don't need to validate JWT again in downstream services, they get request headers from gateway
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User_Id", userId.toString())
                    .header("X-User_Email", username)
                    .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("JWT validation error for path {}: {}", path, e.getMessage());
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    // Mono<Void> -> represent "eventually returns nothing"
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorResponse = String.format(
            "{\"success\":false,\"error\":\"%s\",\"timestamp\":\"%s\"}",
            message,
            java.time.LocalDateTime.now()
        );

        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    public static class Config {
        // Configuration properties (if needed in future)
    }
}
