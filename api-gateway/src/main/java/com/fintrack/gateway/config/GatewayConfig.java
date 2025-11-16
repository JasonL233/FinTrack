package com.fintrack.gateway.config;

import com.fintrack.gateway.security.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    private final AuthenticationFilter authenticationFilter;

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
                .uri("http://localhost:8081"))

            // Transaction Service Routes
            .route("transaction-service", r -> r
                .path("/api/transactions/**")
                .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                .uri("http://localhost:8082"))
            
            .build();
    }
}

