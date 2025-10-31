package com.example.cart.service;

import com.example.cart.entity.Cart;
import com.example.cart.entity.CartItem;
import com.example.cart.repository.CartRepository;
import com.example.cart.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Redis cache keys and TTL
    private static final String CART_CACHE_PREFIX = "cart:";
    private static final String CART_ITEMS_CACHE_PREFIX = "cart-items:";
    private static final String CART_TOTAL_CACHE_PREFIX = "cart-total:";
    private static final String USER_SESSION_PREFIX = "user-session:";
    private static final String PRODUCT_CACHE_PREFIX = "product:";
    
    private static final int CART_CACHE_TTL = 3600; // 1 hour
    private static final int CART_ITEMS_CACHE_TTL = 1800; // 30 minutes
    private static final int USER_SESSION_TTL = 7200; // 2 hours
    private static final int PRODUCT_CACHE_TTL = 1800; // 30 minutes
    
    @Cacheable(value = "cart", key = "#userId")
    public Cart getOrCreateCart(Long userId) {
        // Try to get from cache first
        String cacheKey = CART_CACHE_PREFIX + userId;
        Cart cachedCart = (Cart) redisTemplate.opsForValue().get(cacheKey);
        if (cachedCart != null) {
            return cachedCart;
        }
        
        // Get from database
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        Cart cart = cartOpt.orElseGet(() -> {
            Cart newCart = new Cart(userId);
            return cartRepository.save(newCart);
        });
        
        // Cache the cart with optimized serialization
        cacheCartOptimized(userId, cart);
        
        return cart;
    }
    
    public Cart getCartByUserId(Long userId) {
        return getOrCreateCart(userId);
    }
    
    public Cart addItemToCart(Long userId, Long productId, String productName, String productSku, 
                             Integer quantity, BigDecimal unitPrice) {
        Cart cart = getOrCreateCart(userId);
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findCartItemByCartAndProduct(cart.getId(), productId);
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem(productId, productName, productSku, quantity, unitPrice);
            newItem.setCart(cart);
            cartItemRepository.save(newItem);
        }
        
        // Update cart total
        updateCartTotal(cart);
        
        // Update cache
        updateCartCache(userId, cart);
        
        // Send cart updated event to Kafka
        kafkaTemplate.send("cart-events", "cart-updated", cart);
        
        return cart;
    }
    
    public Cart updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        
        Optional<CartItem> itemOpt = cartItemRepository.findCartItemByCartAndProduct(cart.getId(), productId);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (quantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
            
            // Update cart total
            updateCartTotal(cart);
            
            // Update cache
            updateCartCache(userId, cart);
            
            // Send cart updated event to Kafka
            kafkaTemplate.send("cart-events", "cart-updated", cart);
        }
        
        return cart;
    }
    
    public Cart removeItemFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        
        Optional<CartItem> itemOpt = cartItemRepository.findCartItemByCartAndProduct(cart.getId(), productId);
        if (itemOpt.isPresent()) {
            cartItemRepository.delete(itemOpt.get());
            
            // Update cart total
            updateCartTotal(cart);
            
            // Update cache
            updateCartCache(userId, cart);
            
            // Send cart updated event to Kafka
            kafkaTemplate.send("cart-events", "cart-updated", cart);
        }
        
        return cart;
    }
    
    public Cart clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        
        // Remove all items
        cartItemRepository.deleteByCartId(cart.getId());
        
        // Reset total
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
        
        // Update cache
        updateCartCache(userId, cart);
        
        // Send cart cleared event to Kafka
        kafkaTemplate.send("cart-events", "cart-cleared", cart);
        
        return cart;
    }
    
    public Cart getCart(Long userId) {
        return getOrCreateCart(userId);
    }
    
    public List<CartItem> getCartItems(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCartId(cart.getId());
    }
    
    private void updateCartTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        BigDecimal total = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        cart.setTotalAmount(total);
        cartRepository.save(cart);
    }
    
    private void updateCartCache(Long userId, Cart cart) {
        String cacheKey = CART_CACHE_PREFIX + userId;
        redisTemplate.opsForValue().set(cacheKey, cart, CART_CACHE_TTL, TimeUnit.SECONDS);
    }
    
    public void clearCartCache(Long userId) {
        String cacheKey = CART_CACHE_PREFIX + userId;
        String itemsCacheKey = CART_ITEMS_CACHE_PREFIX + userId;
        String totalCacheKey = CART_TOTAL_CACHE_PREFIX + userId;
        
        // Clear all cart-related cache
        redisTemplate.delete(cacheKey);
        redisTemplate.delete(itemsCacheKey);
        redisTemplate.delete(totalCacheKey);
    }
    
    // Enhanced Redis caching methods for optimal performance
    
    /**
     * Cache cart with optimized serialization and compression
     */
    private void cacheCartOptimized(Long userId, Cart cart) {
        String cacheKey = CART_CACHE_PREFIX + userId;
        String itemsCacheKey = CART_ITEMS_CACHE_PREFIX + userId;
        String totalCacheKey = CART_TOTAL_CACHE_PREFIX + userId;
        
        // Cache cart metadata
        redisTemplate.opsForValue().set(cacheKey, cart, CART_CACHE_TTL, TimeUnit.SECONDS);
        
        // Cache cart items separately for faster access
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            redisTemplate.opsForValue().set(itemsCacheKey, cart.getItems(), CART_ITEMS_CACHE_TTL, TimeUnit.SECONDS);
        }
        
        // Cache cart total for quick calculations
        redisTemplate.opsForValue().set(totalCacheKey, cart.getTotalAmount(), CART_CACHE_TTL, TimeUnit.SECONDS);
    }
    
    /**
     * Get cart items from cache with fallback to database
     */
    @Cacheable(value = "cartItems", key = "#userId")
    public List<CartItem> getCartItemsFromCache(Long userId) {
        String itemsCacheKey = CART_ITEMS_CACHE_PREFIX + userId;
        List<CartItem> cachedItems = (List<CartItem>) redisTemplate.opsForValue().get(itemsCacheKey);
        
        if (cachedItems != null) {
            return cachedItems;
        }
        
        // Fallback to database
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        
        // Cache the items
        redisTemplate.opsForValue().set(itemsCacheKey, items, CART_ITEMS_CACHE_TTL, TimeUnit.SECONDS);
        
        return items;
    }
    
    /**
     * Get cart total from cache with fallback to calculation
     */
    @Cacheable(value = "cartTotal", key = "#userId")
    public BigDecimal getCartTotalFromCache(Long userId) {
        String totalCacheKey = CART_TOTAL_CACHE_PREFIX + userId;
        BigDecimal cachedTotal = (BigDecimal) redisTemplate.opsForValue().get(totalCacheKey);
        
        if (cachedTotal != null) {
            return cachedTotal;
        }
        
        // Calculate total from items
        List<CartItem> items = getCartItemsFromCache(userId);
        BigDecimal total = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Cache the total
        redisTemplate.opsForValue().set(totalCacheKey, total, CART_CACHE_TTL, TimeUnit.SECONDS);
        
        return total;
    }
    
    /**
     * Cache user session for faster authentication
     */
    public void cacheUserSession(Long userId, String sessionId) {
        String sessionCacheKey = USER_SESSION_PREFIX + userId;
        redisTemplate.opsForValue().set(sessionCacheKey, sessionId, USER_SESSION_TTL, TimeUnit.SECONDS);
    }
    
    /**
     * Get user session from cache
     */
    public String getUserSession(Long userId) {
        String sessionCacheKey = USER_SESSION_PREFIX + userId;
        return (String) redisTemplate.opsForValue().get(sessionCacheKey);
    }
    
    /**
     * Cache product information for faster cart operations
     */
    public void cacheProductInfo(Long productId, String productName, BigDecimal price) {
        String productCacheKey = PRODUCT_CACHE_PREFIX + productId;
        // Store product info as hash for efficient access
        redisTemplate.opsForHash().put(productCacheKey, "name", productName);
        redisTemplate.opsForHash().put(productCacheKey, "price", price.toString());
        redisTemplate.expire(productCacheKey, PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
    }
    
    /**
     * Get product info from cache
     */
    public String getCachedProductName(Long productId) {
        String productCacheKey = PRODUCT_CACHE_PREFIX + productId;
        return (String) redisTemplate.opsForHash().get(productCacheKey, "name");
    }
    
    /**
     * Get cached product price
     */
    public BigDecimal getCachedProductPrice(Long productId) {
        String productCacheKey = PRODUCT_CACHE_PREFIX + productId;
        String priceStr = (String) redisTemplate.opsForHash().get(productCacheKey, "price");
        return priceStr != null ? new BigDecimal(priceStr) : null;
    }
    
    /**
     * Batch cache operations for better performance
     */
    public void batchCacheCartItems(Long userId, List<CartItem> items) {
        String itemsCacheKey = CART_ITEMS_CACHE_PREFIX + userId;
        
        // Use pipeline for batch operations
        redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            redisTemplate.opsForValue().set(itemsCacheKey, items, CART_ITEMS_CACHE_TTL, TimeUnit.SECONDS);
            return null;
        });
    }
    
    /**
     * Invalidate cart cache when items change
     */
    @CacheEvict(value = {"cart", "cartItems", "cartTotal"}, key = "#userId")
    public void invalidateCartCache(Long userId) {
        clearCartCache(userId);
    }
    
    /**
     * Warm up cache for frequently accessed carts
     */
    public void warmUpCartCache(List<Long> userIds) {
        userIds.parallelStream().forEach(userId -> {
            try {
                getOrCreateCart(userId);
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Failed to warm up cache for user " + userId + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Get cache statistics for monitoring
     */
    public String getCacheStats() {
        return String.format("Cache Stats - Cart Cache TTL: %ds, Items Cache TTL: %ds, Session TTL: %ds", 
                CART_CACHE_TTL, CART_ITEMS_CACHE_TTL, USER_SESSION_TTL);
    }
}
