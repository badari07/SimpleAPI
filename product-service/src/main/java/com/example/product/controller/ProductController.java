package com.example.product.controller;

import com.example.product.entity.Product;
import com.example.product.entity.Category;
import com.example.product.service.ProductService;
import com.example.product.service.ProductSearchService;
import com.example.product.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductSearchService productSearchService;
    
    @Autowired
    private ElasticsearchService elasticsearchService;
    
    // Product endpoints
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        List<Product> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        try {
            Product product = productService.getProductBySku(sku);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Product>> getProductsBySeller(@PathVariable Long sellerId) {
        List<Product> products = productService.getProductsBySeller(sellerId);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search/page")
    public ResponseEntity<Page<Product>> searchProductsPage(@RequestParam String keyword, Pageable pageable) {
        Page<Product> products = productService.searchProductsPage(keyword, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/available/page")
    public ResponseEntity<Page<Product>> getAvailableProductsPage(Pageable pageable) {
        Page<Product> products = productService.getAvailableProductsPage(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{categoryId}/page")
    public ResponseEntity<Page<Product>> getProductsByCategoryPage(@PathVariable Long categoryId, Pageable pageable) {
        Page<Product> products = productService.getProductsByCategoryPage(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Stock management endpoints
    @GetMapping("/{id}/stock/check")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long id, @RequestParam Integer quantity) {
        boolean inStock = productService.checkStock(id, quantity);
        return ResponseEntity.ok(inStock);
    }
    
    @PutMapping("/{id}/stock/update")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            Product updatedProduct = productService.updateStock(id, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/stock/add")
    public ResponseEntity<Product> addStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            Product updatedProduct = productService.addStock(id, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Category endpoints
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        try {
            Category createdCategory = productService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        List<Category> categories = productService.getRootCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        try {
            Category category = productService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/categories/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubCategories(@PathVariable Long id) {
        List<Category> categories = productService.getSubCategories(id);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String keyword) {
        List<Category> categories = productService.searchCategories(keyword);
        return ResponseEntity.ok(categories);
    }
    
    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        try {
            Category updatedCategory = productService.updateCategory(id, categoryDetails);
            return ResponseEntity.ok(updatedCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            productService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Elasticsearch Search Endpoints
    
    /**
     * Fast product search with Elasticsearch
     */
    @GetMapping("/search/elasticsearch")
    public ResponseEntity<List<Product>> searchProductsElasticsearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        try {
            List<Product> products = productSearchService.searchProducts(keyword, category, minPrice, maxPrice, sortBy, sortOrder);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search products with typo correction and suggestions
     */
    @GetMapping("/search/suggestions")
    public ResponseEntity<List<Product>> searchProductsWithSuggestions(@RequestParam String keyword) {
        try {
            List<Product> products = productSearchService.searchProductsWithSuggestions(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get product suggestions based on partial input
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getProductSuggestions(@RequestParam String partialKeyword) {
        try {
            List<String> suggestions = productSearchService.getProductSuggestions(partialKeyword);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search products by category with Elasticsearch
     */
    @GetMapping("/search/category")
    public ResponseEntity<List<Product>> searchProductsByCategory(@RequestParam String categoryName) {
        try {
            List<Product> products = productSearchService.searchProductsByCategory(categoryName);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get trending products
     */
    @GetMapping("/trending")
    public ResponseEntity<List<Product>> getTrendingProducts(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Product> products = productSearchService.getTrendingProducts(limit);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get search analytics
     */
    @GetMapping("/search/analytics")
    public ResponseEntity<Map<String, Object>> getSearchAnalytics() {
        try {
            Map<String, Object> analytics = productSearchService.getSearchAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Clear search cache
     */
    @PostMapping("/search/clear-cache")
    public ResponseEntity<Map<String, String>> clearSearchCache() {
        try {
            productSearchService.clearSearchCache();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Search cache cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Warm up search cache
     */
    @PostMapping("/search/warm-up")
    public ResponseEntity<Map<String, String>> warmUpSearchCache() {
        try {
            productSearchService.warmUpSearchCache();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Search cache warmed up successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
