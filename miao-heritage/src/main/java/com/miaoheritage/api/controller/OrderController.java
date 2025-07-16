package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.CreateOrderRequest;
import com.miaoheritage.api.dto.MessageResponse;
import com.miaoheritage.api.dto.OrderDTO;
import com.miaoheritage.entity.Order;
import com.miaoheritage.entity.Order.OrderStatus;
import com.miaoheritage.entity.Order.PaymentMethod;
import com.miaoheritage.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 获取当前用户的订单列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCurrentUserOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                orderPage = orderService.getCurrentUserOrdersByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("无效的订单状态: {}", status);
                orderPage = orderService.getCurrentUserOrders(pageable);
            }
        } else {
            orderPage = orderService.getCurrentUserOrders(pageable);
        }
        
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOs);
        response.put("currentPage", orderPage.getNumber());
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable Long id) {
        return orderService.findById(id)
                .map(order -> ResponseEntity.ok(OrderDTO.fromEntity(order)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取订单详情（通过订单号）
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(order -> ResponseEntity.ok(OrderDTO.fromEntity(order)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        try {
            // 解析支付方式
            PaymentMethod paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod());
            
            // 从购物车创建订单
            Order order = orderService.createOrderFromCart(
                    request.getShippingAddress(),
                    request.getShippingName(),
                    request.getShippingPhone(),
                    paymentMethod
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(OrderDTO.fromEntity(order));
        } catch (IllegalArgumentException e) {
            log.error("创建订单失败: 无效的支付方式: {}", request.getPaymentMethod());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("创建订单失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 处理订单支付
     */
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> processPayment(
            @PathVariable Long id,
            @RequestParam String paymentId) {
        
        try {
            Order order = orderService.processPayment(id, paymentId);
            return ResponseEntity.ok(OrderDTO.fromEntity(order));
        } catch (RuntimeException e) {
            log.error("处理订单支付失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        try {
            Order order = orderService.cancelOrder(id);
            return ResponseEntity.ok(OrderDTO.fromEntity(order));
        } catch (RuntimeException e) {
            log.error("取消订单失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新订单状态（管理员用）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            Order order = orderService.updateOrderStatus(id, orderStatus);
            return ResponseEntity.ok(OrderDTO.fromEntity(order));
        } catch (IllegalArgumentException e) {
            log.error("更新订单状态失败: 无效的状态: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("更新订单状态失败", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * 获取所有订单（管理员用）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status);
                orderPage = orderService.getOrdersByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("无效的订单状态: {}", status);
                orderPage = orderService.getAllOrders(pageable);
            }
        } else {
            orderPage = orderService.getAllOrders(pageable);
        }
        
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOs);
        response.put("currentPage", orderPage.getNumber());
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
} 