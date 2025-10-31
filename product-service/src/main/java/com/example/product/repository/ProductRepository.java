package com.example.product.repository;

import com.example.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByStatus(Product.ProductStatus status);
    
    List<Product> findByCategoryId(Long categoryId);
    
    List<Product> findBySellerId(Long sellerId);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0")
    List<Product> findAvailableProducts();
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 AND p.category.id = :categoryId")
    List<Product> findAvailableProductsByCategory(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 AND " +
           "p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findProductsByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                          @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0")
    Page<Product> findAvailableProductsPage(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProductsPage(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 AND p.category.id = :categoryId")
    Page<Product> findAvailableProductsByCategoryPage(@Param("categoryId") Long categoryId, Pageable pageable);
    
    boolean existsBySku(String sku);
    
    boolean existsByIdAndStockQuantityGreaterThanEqual(Long id, Integer quantity);
}
