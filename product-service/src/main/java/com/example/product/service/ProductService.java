package com.example.product.service;

import com.example.product.entity.Product;
import com.example.product.entity.Category;
import com.example.product.repository.ProductRepository;
import com.example.product.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Product CRUD operations
    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new RuntimeException("Product with SKU " + product.getSku() + " already exists");
        }
        
        Product savedProduct = productRepository.save(product);
        
        // Send product created event to Kafka
        kafkaTemplate.send("product-events", "product-created", savedProduct);
        
        return savedProduct;
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setSku(productDetails.getSku());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStatus(productDetails.getStatus());
        product.setCategory(productDetails.getCategory());
        product.setSellerId(productDetails.getSellerId());
        
        Product updatedProduct = productRepository.save(product);
        
        // Send product updated event to Kafka
        kafkaTemplate.send("product-events", "product-updated", updatedProduct);
        
        return updatedProduct;
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        productRepository.delete(product);
        
        // Send product deleted event to Kafka
        kafkaTemplate.send("product-events", "product-deleted", product);
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findAvailableProductsByCategory(categoryId);
    }
    
    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }
    
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findProductsByPriceRange(minPrice, maxPrice);
    }
    
    public Page<Product> getAvailableProductsPage(Pageable pageable) {
        return productRepository.findAvailableProductsPage(pageable);
    }
    
    public Page<Product> searchProductsPage(String keyword, Pageable pageable) {
        return productRepository.searchProductsPage(keyword, pageable);
    }
    
    public Page<Product> getProductsByCategoryPage(Long categoryId, Pageable pageable) {
        return productRepository.findAvailableProductsByCategoryPage(categoryId, pageable);
    }
    
    // Stock management
    public boolean checkStock(Long productId, Integer quantity) {
        return productRepository.existsByIdAndStockQuantityGreaterThanEqual(productId, quantity);
    }
    
    public Product updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        
        // Update status if out of stock
        if (product.getStockQuantity() == 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        }
        
        Product updatedProduct = productRepository.save(product);
        
        // Send stock updated event to Kafka
        kafkaTemplate.send("product-events", "stock-updated", updatedProduct);
        
        return updatedProduct;
    }
    
    public Product addStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        product.setStockQuantity(product.getStockQuantity() + quantity);
        
        // Update status if back in stock
        if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK && product.getStockQuantity() > 0) {
            product.setStatus(Product.ProductStatus.ACTIVE);
        }
        
        Product updatedProduct = productRepository.save(product);
        
        // Send stock updated event to Kafka
        kafkaTemplate.send("product-events", "stock-updated", updatedProduct);
        
        return updatedProduct;
    }
    
    // Category operations
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setImageUrl(categoryDetails.getImageUrl());
        category.setParent(categoryDetails.getParent());
        
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        categoryRepository.delete(category);
    }
    
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }
    
    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findSubCategories(parentId);
    }
    
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.searchCategories(keyword);
    }
}
