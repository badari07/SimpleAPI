package com.example.product.service;

import com.example.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ProductSearchService {
    
    @Autowired
    private ElasticsearchService elasticsearchService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String SEARCH_CACHE_PREFIX = "search:";
    private static final String TRENDING_CACHE_PREFIX = "trending:";
    private static final String SUGGESTIONS_CACHE_PREFIX = "suggestions:";
    private static final int SEARCH_CACHE_TTL = 900; // 15 minutes
    private static final int TRENDING_CACHE_TTL = 3600; // 1 hour
    private static final int SUGGESTIONS_CACHE_TTL = 1800; // 30 minutes
    
    /**
     * Fast product search with Elasticsearch and Redis caching
     */
    @Cacheable(value = "productSearch", key = "#keyword + '_' + #category + '_' + #minPrice + '_' + #maxPrice + '_' + #sortBy + '_' + #sortOrder")
    public List<Product> searchProducts(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String sortOrder) {
        try {
            // Send search event to Kafka for analytics
            Map<String, Object> searchEvent = new HashMap<>();
            searchEvent.put("keyword", keyword);
            searchEvent.put("category", category);
            searchEvent.put("minPrice", minPrice);
            searchEvent.put("maxPrice", maxPrice);
            searchEvent.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("search-events", "product-search", searchEvent);
            
            // Search using Elasticsearch
            List<Product> results = elasticsearchService.searchProducts(keyword, 0, 20);
            
            // Cache the results
            String cacheKey = SEARCH_CACHE_PREFIX + generateSearchKey(keyword, category, minPrice, maxPrice, sortBy, sortOrder);
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_CACHE_TTL, TimeUnit.SECONDS);
            
            return results;
            
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Search products with typo correction and suggestions
     */
    @Cacheable(value = "productSearchWithSuggestions", key = "#keyword")
    public List<Product> searchProductsWithSuggestions(String keyword) {
        try {
            // Send search event to Kafka
            Map<String, Object> searchEvent = new HashMap<>();
            searchEvent.put("keyword", keyword);
            searchEvent.put("type", "suggestions");
            searchEvent.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("search-events", "product-search-suggestions", searchEvent);
            
            // Search using Elasticsearch
            Map<String, Object> result = elasticsearchService.searchProductsWithSuggestions(keyword, 0, 20);
            List<Product> results = (List<Product>) result.get("products");
            
            // Cache the results
            String cacheKey = SEARCH_CACHE_PREFIX + "suggestions:" + keyword;
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_CACHE_TTL, TimeUnit.SECONDS);
            
            return results;
            
        } catch (Exception e) {
            System.err.println("Search with suggestions failed: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get product suggestions based on partial input
     */
    @Cacheable(value = "productSuggestions", key = "#partialKeyword")
    public List<String> getProductSuggestions(String partialKeyword) {
        try {
            if (partialKeyword == null || partialKeyword.trim().isEmpty()) {
                return List.of();
            }
            
            // Check cache first
            String cacheKey = SUGGESTIONS_CACHE_PREFIX + partialKeyword;
            @SuppressWarnings("unchecked")
            List<String> cachedSuggestions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedSuggestions != null) {
                return cachedSuggestions;
            }
            
            // Get suggestions from Elasticsearch
            List<String> suggestions = elasticsearchService.getProductSuggestions(partialKeyword);
            
            // Cache the suggestions
            redisTemplate.opsForValue().set(cacheKey, suggestions, SUGGESTIONS_CACHE_TTL, TimeUnit.SECONDS);
            
            return suggestions;
            
        } catch (Exception e) {
            System.err.println("Failed to get product suggestions: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Search products by category
     */
    @Cacheable(value = "productsByCategory", key = "#categoryName")
    public List<Product> searchProductsByCategory(String categoryName) {
        try {
            // Send search event to Kafka
            Map<String, Object> searchEvent = new HashMap<>();
            searchEvent.put("category", categoryName);
            searchEvent.put("type", "category-search");
            searchEvent.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("search-events", "category-search", searchEvent);
            
            // Search using Elasticsearch
            List<Product> results = elasticsearchService.searchProductsByCategory(categoryName, 0, 20);
            
            // Cache the results
            String cacheKey = SEARCH_CACHE_PREFIX + "category:" + categoryName;
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_CACHE_TTL, TimeUnit.SECONDS);
            
            return results;
            
        } catch (Exception e) {
            System.err.println("Category search failed: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get trending products
     */
    @Cacheable(value = "trendingProducts", key = "#limit")
    public List<Product> getTrendingProducts(int limit) {
        try {
            // Check cache first
            String cacheKey = TRENDING_CACHE_PREFIX + limit;
            @SuppressWarnings("unchecked")
            List<Product> cachedTrending = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedTrending != null) {
                return cachedTrending;
            }
            
            // Get trending products from Elasticsearch
            List<Product> trending = elasticsearchService.getTrendingProducts(limit);
            
            // Cache the results
            redisTemplate.opsForValue().set(cacheKey, trending, TRENDING_CACHE_TTL, TimeUnit.SECONDS);
            
            return trending;
            
        } catch (Exception e) {
            System.err.println("Failed to get trending products: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get search analytics
     */
    public Map<String, Object> getSearchAnalytics() {
        try {
            Map<String, Object> analytics = elasticsearchService.getSearchAnalytics();
            
            // Add cache statistics
            analytics.put("cacheStats", getCacheStatistics());
            
            return analytics;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get search analytics: " + e.getMessage());
            return error;
        }
    }
    
    /**
     * Clear search cache
     */
    @CacheEvict(value = {"productSearch", "productSearchWithSuggestions", "productSuggestions", "productsByCategory", "trendingProducts"}, allEntries = true)
    public void clearSearchCache() {
        try {
            // Clear Redis cache
            redisTemplate.delete(redisTemplate.keys(SEARCH_CACHE_PREFIX + "*"));
            redisTemplate.delete(redisTemplate.keys(TRENDING_CACHE_PREFIX + "*"));
            redisTemplate.delete(redisTemplate.keys(SUGGESTIONS_CACHE_PREFIX + "*"));
            
            System.out.println("Search cache cleared successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to clear search cache: " + e.getMessage());
        }
    }
    
    /**
     * Warm up search cache
     */
    public void warmUpSearchCache() {
        try {
            // Pre-load trending products
            getTrendingProducts(10);
            
            // Pre-load common category searches
            String[] commonCategories = {"electronics", "clothing", "books", "home", "sports"};
            for (String category : commonCategories) {
                searchProductsByCategory(category);
            }
            
            System.out.println("Search cache warmed up successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to warm up search cache: " + e.getMessage());
        }
    }
    
    /**
     * Generate search cache key
     */
    private String generateSearchKey(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String sortOrder) {
        return String.format("%s_%s_%s_%s_%s_%s", 
                keyword != null ? keyword : "null",
                category != null ? category : "null",
                minPrice != null ? minPrice.toString() : "null",
                maxPrice != null ? maxPrice.toString() : "null",
                sortBy != null ? sortBy : "null",
                sortOrder != null ? sortOrder : "null");
    }
    
    /**
     * Get cache statistics
     */
    private Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Count search cache keys
            var searchKeys = redisTemplate.keys(SEARCH_CACHE_PREFIX + "*");
            stats.put("searchCacheKeys", searchKeys != null ? searchKeys.size() : 0);
            
            // Count trending cache keys
            var trendingKeys = redisTemplate.keys(TRENDING_CACHE_PREFIX + "*");
            stats.put("trendingCacheKeys", trendingKeys != null ? trendingKeys.size() : 0);
            
            // Count suggestions cache keys
            var suggestionsKeys = redisTemplate.keys(SUGGESTIONS_CACHE_PREFIX + "*");
            stats.put("suggestionsCacheKeys", suggestionsKeys != null ? suggestionsKeys.size() : 0);
            
        } catch (Exception e) {
            stats.put("error", "Failed to get cache statistics: " + e.getMessage());
        }
        
        return stats;
    }
}
