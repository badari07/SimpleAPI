package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/users/register",
            "/api/users/login",
            "/api/users/forgot-password",
            "/api/users/reset-password",
            "/api/products",
            "/api/products/search",
            "/api/products/categories",
            "/health"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange);
        }
        
        // Extract token
        String token = authHeader.substring(7);
        
        // Validate token (in a real implementation, this would validate JWT)
        if (isValidToken(token)) {
            // Add user info to request headers
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", extractUserIdFromToken(token))
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } else {
            return handleUnauthorized(exchange);
        }
    }
    
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    private boolean isValidToken(String token) {
        // In a real implementation, this would validate JWT token
        // For now, we'll accept any non-empty token
        return token != null && !token.isEmpty() && !token.equals("invalid");
    }
    
    private String extractUserIdFromToken(String token) {
        // In a real implementation, this would extract user ID from JWT
        // For now, we'll return a default user ID
        return "1";
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing authentication token\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
    
    @Override
    public int getOrder() {
        return -1; // High priority
    }
}
