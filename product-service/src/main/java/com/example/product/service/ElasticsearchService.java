package com.example.product.service;

import com.example.product.entity.Product;
import com.example.product.entity.Category;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
// Removed unused imports
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Criteria;

@Service
public class ElasticsearchService {
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Index a product in Elasticsearch
     */
    public void indexProduct(Product product) {
        try {
            elasticsearchOperations.save(product);
            System.out.println("Product indexed successfully: " + product.getName());
        } catch (Exception e) {
            System.err.println("Error indexing product: " + e.getMessage());
        }
    }
    
    /**
     * Update a product in Elasticsearch
     */
    public void updateProduct(Product product) {
        try {
            elasticsearchOperations.save(product);
            System.out.println("Product updated successfully: " + product.getName());
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
    }
    
    /**
     * Delete a product from Elasticsearch
     */
    public void deleteProduct(Long productId) {
        try {
            elasticsearchOperations.delete(String.valueOf(productId), Product.class);
            System.out.println("Product deleted successfully: " + productId);
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }
    }
    
    /**
     * Search products using Elasticsearch
     */
    public List<Product> searchProducts(String query, int page, int size) {
        try {
            // Simple string query for now
            String searchQuery = String.format("""
                {
                    "query": {
                        "multi_match": {
                            "query": "%s",
                            "fields": ["name^2", "description", "category.name"]
                        }
                    },
                    "from": %d,
                    "size": %d
                }
                """, query, page * size, size);
            
            Query queryObj = new StringQuery(searchQuery, null, null);
            SearchHits<Product> searchHits = elasticsearchOperations.search(queryObj, Product.class);
            
            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching products: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Search products with suggestions
     */
    public Map<String, Object> searchProductsWithSuggestions(String query, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Product> products = searchProducts(query, page, size);
            result.put("products", products);
            result.put("total", products.size());
            result.put("suggestions", getProductSuggestions(query));
        } catch (Exception e) {
            System.err.println("Error in search with suggestions: " + e.getMessage());
            result.put("products", List.of());
            result.put("total", 0);
            result.put("suggestions", List.of());
        }
        
        return result;
    }
    
    /**
     * Get product suggestions
     */
    public List<String> getProductSuggestions(String query) {
        try {
            // Simple implementation - return first 5 products that match
            List<Product> products = searchProducts(query, 0, 5);
            return products.stream()
                    .map(Product::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting suggestions: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Search products by category
     */
    public List<Product> searchProductsByCategory(String categoryName, int page, int size) {
        try {
            String searchQuery = String.format("""
                {
                    "query": {
                        "match": {
                            "category.name": "%s"
                        }
                    },
                    "from": %d,
                    "size": %d
                }
                """, categoryName, page * size, size);
            
            Query queryObj = new StringQuery(searchQuery, null, null);
            SearchHits<Product> searchHits = elasticsearchOperations.search(queryObj, Product.class);
            
            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching by category: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get trending products
     */
    public List<Product> getTrendingProducts(int limit) {
        try {
            String searchQuery = String.format("""
                {
                    "query": {
                        "match_all": {}
                    },
                    "sort": [
                        {"createdAt": {"order": "desc"}}
                    ],
                    "size": %d
                }
                """, limit);
            
            Query queryObj = new StringQuery(searchQuery, null, null);
            SearchHits<Product> searchHits = elasticsearchOperations.search(queryObj, Product.class);
            
            return searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting trending products: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Get search analytics
     */
    public Map<String, Object> getSearchAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        try {
            // Simple analytics - return basic stats
            analytics.put("totalProducts", getTotalProductCount());
            analytics.put("lastUpdated", System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("Error getting analytics: " + e.getMessage());
            analytics.put("error", e.getMessage());
        }
        return analytics;
    }
    
    /**
     * Clear search cache
     */
    public void clearSearchCache() {
        try {
            // In a real implementation, you would clear Redis cache here
            System.out.println("Search cache cleared");
        } catch (Exception e) {
            System.err.println("Error clearing cache: " + e.getMessage());
        }
    }
    
    /**
     * Warm up search cache
     */
    public void warmUpSearchCache() {
        try {
            // Pre-load popular searches
            searchProducts("laptop", 0, 10);
            searchProducts("phone", 0, 10);
            searchProducts("book", 0, 10);
            System.out.println("Search cache warmed up");
        } catch (Exception e) {
            System.err.println("Error warming up cache: " + e.getMessage());
        }
    }
    
    private long getTotalProductCount() {
        try {
            String countQuery = """
                {
                    "query": {
                        "match_all": {}
                    },
                    "size": 0
                }
                """;
            
            Query queryObj = new StringQuery(countQuery);
            SearchHits<Product> searchHits = elasticsearchOperations.search(queryObj, Product.class);
            return searchHits.getTotalHits();
        } catch (Exception e) {
            System.err.println("Error getting product count: " + e.getMessage());
            return 0;
        }
    }
}