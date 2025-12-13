package com.fintrack.gateway.config;

import com.fintrack.gateway.security.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    private final AuthenticationFilter authenticationFilter;

    @Value("${USER_SERVICE_URL:http://user-service:8081}")
    private String userServiceUrl;

    @Value("${TRANSACTION_SERVICE_URL:http://transaction-service:8082}")
    private String transactionServiceUrl;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Service Routes
            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                .uri(userServiceUrl))

            // Transaction Service Routes
            .route("transaction-service", r -> r
                .path("/api/transactions/**")
                .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                .uri(transactionServiceUrl))
            
            .build();
    }
}

