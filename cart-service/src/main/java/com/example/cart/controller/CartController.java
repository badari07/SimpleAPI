package com.example.cart.controller;

import com.example.cart.entity.Cart;
import com.example.cart.entity.CartItem;
import com.example.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        Cart cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }
    
    @GetMapping("/{userId}/items")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable Long userId) {
        List<CartItem> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(items);
    }
    
    @PostMapping("/{userId}/items")
    public ResponseEntity<Cart> addItemToCart(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam String productName,
            @RequestParam String productSku,
            @RequestParam Integer quantity,
            @RequestParam BigDecimal unitPrice) {
        
        try {
            Cart cart = cartService.addItemToCart(userId, productId, productName, productSku, quantity, unitPrice);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<Cart> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        
        try {
            Cart cart = cartService.updateCartItemQuantity(userId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        
        try {
            Cart cart = cartService.removeItemFromCart(userId, productId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<Cart> clearCart(@PathVariable Long userId) {
        try {
            Cart cart = cartService.clearCart(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> checkoutCart(@PathVariable Long userId) {
        try {
            // This would typically trigger the order creation process
            // For now, we'll just clear the cart and send a checkout event
            Cart cart = cartService.getCart(userId);
            cartService.clearCart(userId);
            
            // Send checkout event to Kafka
            // This would be consumed by the Order Service
            return ResponseEntity.ok("Cart checkout initiated for user: " + userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
