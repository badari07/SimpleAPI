package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        System.out.println("=== API Gateway Request ===");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Method: " + request.getMethod());
        System.out.println("Path: " + request.getURI().getPath());
        System.out.println("Query: " + request.getURI().getQuery());
        System.out.println("Headers: " + request.getHeaders());
        System.out.println("Remote Address: " + request.getRemoteAddress());
        System.out.println("==========================");
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            System.out.println("=== API Gateway Response ===");
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Headers: " + response.getHeaders());
            System.out.println("============================");
        }));
    }
    
    @Override
    public int getOrder() {
        return 1; // After rate limiting
    }
}
