package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.entity.ShippingAddress;
import com.example.order.repository.OrderRepository;
import com.example.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public Order createOrder(Long userId, List<OrderItem> items, ShippingAddress shippingAddress) {
        // Calculate total amount
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create order
        Order order = new Order(userId, totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setStatus(Order.OrderStatus.PENDING);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Save order items
        for (OrderItem item : items) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }
        
        // Send order created event to Kafka
        kafkaTemplate.send("order-events", "order-created", savedOrder);
        
        return savedOrder;
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        // Send order status updated event to Kafka
        kafkaTemplate.send("order-events", "order-status-updated", updatedOrder);
        
        return updatedOrder;
    }
    
    public Order updateOrderPayment(Long orderId, Long paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.setPaymentId(paymentId);
        Order updatedOrder = orderRepository.save(order);
        
        // Send order payment updated event to Kafka
        kafkaTemplate.send("order-events", "order-payment-updated", updatedOrder);
        
        return updatedOrder;
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }
    
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }
    
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findUserOrdersByDateDesc(userId);
    }
    
    public List<Order> getOrdersByUserIdAndStatus(Long userId, Order.OrderStatus status) {
        return orderRepository.findUserOrdersByStatus(userId, status);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findOrderItemsByOrderId(orderId);
    }
    
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        
        // Send order cancelled event to Kafka
        kafkaTemplate.send("order-events", "order-cancelled", updatedOrder);
        
        return updatedOrder;
    }
    
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Cannot confirm order with status: " + order.getStatus());
        }
        
        order.setStatus(Order.OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);
        
        // Send order confirmed event to Kafka
        kafkaTemplate.send("order-events", "order-confirmed", updatedOrder);
        
        return updatedOrder;
    }
    
    public Order shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Cannot ship order with status: " + order.getStatus());
        }
        
        order.setStatus(Order.OrderStatus.SHIPPED);
        Order updatedOrder = orderRepository.save(order);
        
        // Send order shipped event to Kafka
        kafkaTemplate.send("order-events", "order-shipped", updatedOrder);
        
        return updatedOrder;
    }
    
    public Order deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new RuntimeException("Cannot deliver order with status: " + order.getStatus());
        }
        
        order.setStatus(Order.OrderStatus.DELIVERED);
        Order updatedOrder = orderRepository.save(order);
        
        // Send order delivered event to Kafka
        kafkaTemplate.send("order-events", "order-delivered", updatedOrder);
        
        return updatedOrder;
    }
}
