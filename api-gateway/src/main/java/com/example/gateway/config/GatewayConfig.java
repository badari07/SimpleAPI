package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r.path("/api/users/**")
                        .uri("http://localhost:8081"))
                
                // Product Service Routes
                .route("product-service", r -> r.path("/api/products/**")
                        .uri("http://localhost:8082"))
                
                // Cart Service Routes
                .route("cart-service", r -> r.path("/api/cart/**")
                        .uri("http://localhost:8083"))
                
                // Order Service Routes
                .route("order-service", r -> r.path("/api/orders/**")
                        .uri("http://localhost:8084"))
                
                // Payment Service Routes
                .route("payment-service", r -> r.path("/api/payments/**")
                        .uri("http://localhost:8085"))
                
                // Notification Service Routes
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .uri("http://localhost:8086"))
                
                // Health Check Routes
                .route("user-service-health", r -> r.path("/health/users")
                        .uri("http://localhost:8081/actuator/health"))
                
                .route("product-service-health", r -> r.path("/health/products")
                        .uri("http://localhost:8082/actuator/health"))
                
                .route("cart-service-health", r -> r.path("/health/cart")
                        .uri("http://localhost:8083/actuator/health"))
                
                .route("order-service-health", r -> r.path("/health/orders")
                        .uri("http://localhost:8084/actuator/health"))
                
                .route("payment-service-health", r -> r.path("/health/payments")
                        .uri("http://localhost:8085/actuator/health"))
                
                .route("notification-service-health", r -> r.path("/health/notifications")
                        .uri("http://localhost:8086/actuator/health"))
                
                .build();
    }
}
