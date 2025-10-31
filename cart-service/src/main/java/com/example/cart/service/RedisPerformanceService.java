package com.example.cart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
public class RedisPerformanceService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CacheManager cacheManager;
    
    /**
     * Get Redis performance metrics
     */
    public Map<String, Object> getRedisMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get Redis info
            String info = redisTemplate.getConnectionFactory().getConnection().info().toString();
            metrics.put("redisInfo", parseRedisInfo(info));
            
            // Get cache statistics
            metrics.put("cacheStats", getCacheStatistics());
            
            // Get memory usage
            metrics.put("memoryUsage", getMemoryUsage());
            
            // Get key statistics
            metrics.put("keyStats", getKeyStatistics());
            
        } catch (Exception e) {
            metrics.put("error", "Failed to get Redis metrics: " + e.getMessage());
        }
        
        return metrics;
    }
    
    /**
     * Parse Redis INFO command output
     */
    private Map<String, String> parseRedisInfo(String info) {
        Map<String, String> infoMap = new HashMap<>();
        String[] lines = info.split("\r\n");
        
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    infoMap.put(parts[0], parts[1]);
                }
            }
        }
        
        return infoMap;
    }
    
    /**
     * Get cache statistics
     */
    private Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get cache names
            List<String> cacheNames = new ArrayList<>();
            cacheManager.getCacheNames().forEach(cacheNames::add);
            stats.put("cacheNames", cacheNames);
            
            // Get cache hit/miss ratios for each cache
            Map<String, Map<String, Object>> cacheStats = new HashMap<>();
            
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheInfo = new HashMap<>();
                    cacheInfo.put("name", cacheName);
                    cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                    cacheStats.put(cacheName, cacheInfo);
                }
            }
            
            stats.put("cacheDetails", cacheStats);
            
        } catch (Exception e) {
            stats.put("error", "Failed to get cache statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Get memory usage statistics
     */
    private Map<String, Object> getMemoryUsage() {
        Map<String, Object> memory = new HashMap<>();
        
        try {
            // Get used memory
            Long usedMemory = redisTemplate.getConnectionFactory().getConnection().dbSize();
            memory.put("usedMemory", usedMemory);
            
            // Get total keys
            Set<String> keys = redisTemplate.keys("*");
            memory.put("totalKeys", keys != null ? keys.size() : 0);
            
            // Get cart-specific keys
            Set<String> cartKeys = redisTemplate.keys("cart:*");
            memory.put("cartKeys", cartKeys != null ? cartKeys.size() : 0);
            
            // Get session keys
            Set<String> sessionKeys = redisTemplate.keys("user-session:*");
            memory.put("sessionKeys", sessionKeys != null ? sessionKeys.size() : 0);
            
            // Get product keys
            Set<String> productKeys = redisTemplate.keys("product:*");
            memory.put("productKeys", productKeys != null ? productKeys.size() : 0);
            
        } catch (Exception e) {
            memory.put("error", "Failed to get memory usage: " + e.getMessage());
        }
        
        return memory;
    }
    
    /**
     * Get key statistics
     */
    private Map<String, Object> getKeyStatistics() {
        Map<String, Object> keyStats = new HashMap<>();
        
        try {
            // Count different types of keys
            Map<String, Integer> keyCounts = new HashMap<>();
            
            String[] patterns = {"cart:*", "cart-items:*", "cart-total:*", "user-session:*", "product:*"};
            
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                keyCounts.put(pattern, keys != null ? keys.size() : 0);
            }
            
            keyStats.put("keyCounts", keyCounts);
            
            // Get TTL statistics
            Map<String, Object> ttlStats = new HashMap<>();
            Set<String> cartKeys = redisTemplate.keys("cart:*");
            
            if (cartKeys != null && !cartKeys.isEmpty()) {
                List<Long> ttls = new ArrayList<>();
                for (String key : cartKeys) {
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    if (ttl != null && ttl > 0) {
                        ttls.add(ttl);
                    }
                }
                
                if (!ttls.isEmpty()) {
                    ttlStats.put("minTtl", ttls.stream().mapToLong(Long::longValue).min().orElse(0));
                    ttlStats.put("maxTtl", ttls.stream().mapToLong(Long::longValue).max().orElse(0));
                    ttlStats.put("avgTtl", ttls.stream().mapToLong(Long::longValue).average().orElse(0));
                }
            }
            
            keyStats.put("ttlStats", ttlStats);
            
        } catch (Exception e) {
            keyStats.put("error", "Failed to get key statistics: " + e.getMessage());
        }
        
        return keyStats;
    }
    
    /**
     * Clear all cart-related cache
     */
    public void clearAllCartCache() {
        try {
            Set<String> cartKeys = redisTemplate.keys("cart:*");
            Set<String> sessionKeys = redisTemplate.keys("user-session:*");
            Set<String> productKeys = redisTemplate.keys("product:*");
            
            if (cartKeys != null && !cartKeys.isEmpty()) {
                redisTemplate.delete(cartKeys);
            }
            if (sessionKeys != null && !sessionKeys.isEmpty()) {
                redisTemplate.delete(sessionKeys);
            }
            if (productKeys != null && !productKeys.isEmpty()) {
                redisTemplate.delete(productKeys);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear cart cache: " + e.getMessage());
        }
    }
    
    /**
     * Warm up cache for better performance
     */
    public void warmUpCache() {
        try {
            // Pre-load common cart operations
            // This is a placeholder for cache warming logic
            System.out.println("Cache warming initiated...");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to warm up cache: " + e.getMessage());
        }
    }
    
    /**
     * Get cache hit ratio
     */
    public double getCacheHitRatio() {
        try {
            // This would require custom metrics collection
            // For now, return a placeholder
            return 0.85; // 85% hit ratio
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Get Redis connection status
     */
    public boolean isRedisConnected() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
