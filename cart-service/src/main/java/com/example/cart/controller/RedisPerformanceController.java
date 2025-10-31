package com.example.cart.controller;

import com.example.cart.service.RedisPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart/redis")
public class RedisPerformanceController {
    
    @Autowired
    private RedisPerformanceService redisPerformanceService;
    
    /**
     * Get Redis performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getRedisMetrics() {
        try {
            Map<String, Object> metrics = redisPerformanceService.getRedisMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get Redis metrics: " + e.getMessage()));
        }
    }
    
    /**
     * Get Redis connection status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRedisStatus() {
        try {
            boolean isConnected = redisPerformanceService.isRedisConnected();
            return ResponseEntity.ok(Map.of(
                    "connected", isConnected,
                    "status", isConnected ? "UP" : "DOWN"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to check Redis status: " + e.getMessage()));
        }
    }
    
    /**
     * Get cache hit ratio
     */
    @GetMapping("/hit-ratio")
    public ResponseEntity<Map<String, Object>> getCacheHitRatio() {
        try {
            double hitRatio = redisPerformanceService.getCacheHitRatio();
            return ResponseEntity.ok(Map.of(
                    "hitRatio", hitRatio,
                    "percentage", String.format("%.2f%%", hitRatio * 100)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get cache hit ratio: " + e.getMessage()));
        }
    }
    
    /**
     * Clear all cart-related cache
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, Object>> clearCartCache() {
        try {
            redisPerformanceService.clearAllCartCache();
            return ResponseEntity.ok(Map.of(
                    "message", "Cart cache cleared successfully",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to clear cart cache: " + e.getMessage()));
        }
    }
    
    /**
     * Warm up cache for better performance
     */
    @PostMapping("/warm-up")
    public ResponseEntity<Map<String, Object>> warmUpCache() {
        try {
            redisPerformanceService.warmUpCache();
            return ResponseEntity.ok(Map.of(
                    "message", "Cache warming initiated",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to warm up cache: " + e.getMessage()));
        }
    }
    
    /**
     * Get Redis health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getRedisHealth() {
        try {
            boolean isConnected = redisPerformanceService.isRedisConnected();
            double hitRatio = redisPerformanceService.getCacheHitRatio();
            
            Map<String, Object> health = Map.of(
                    "redis", Map.of(
                            "connected", isConnected,
                            "status", isConnected ? "UP" : "DOWN"
                    ),
                    "cache", Map.of(
                            "hitRatio", hitRatio,
                            "performance", hitRatio > 0.8 ? "EXCELLENT" : 
                                          hitRatio > 0.6 ? "GOOD" : 
                                          hitRatio > 0.4 ? "FAIR" : "POOR"
                    ),
                    "overall", isConnected && hitRatio > 0.6 ? "HEALTHY" : "UNHEALTHY"
            );
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get Redis health: " + e.getMessage()));
        }
    }
}

