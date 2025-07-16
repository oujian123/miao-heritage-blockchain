package com.miaoheritage.service;

import com.miaoheritage.blockchain.service.BlockchainService;
import com.miaoheritage.entity.*;
import com.miaoheritage.entity.Order.OrderStatus;
import com.miaoheritage.entity.Order.PaymentMethod;
import com.miaoheritage.repository.OrderRepository;
import com.miaoheritage.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 获取当前用户的订单列表
     */
    Page<Order> getCurrentUserOrders(Pageable pageable);
    
    /**
     * 获取指定状态的订单
     */
    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * 获取当前用户指定状态的订单
     */
    Page<Order> getCurrentUserOrdersByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * 根据ID获取订单详情
     */
    Order getOrderById(Long orderId);
    
    /**
     * 根据订单号获取订单
     */
    Order getOrderByNumber(String orderNumber);
    
    /**
     * 从购物车创建订单
     */
    Order createOrderFromCart(String shippingAddress, String shippingName, 
                             String shippingPhone, PaymentMethod paymentMethod);
    
    /**
     * 更新订单状态
     */
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    
    /**
     * 处理订单支付
     */
    Order processPayment(Long orderId, String paymentId);
    
    /**
     * 取消订单
     */
    Order cancelOrder(Long orderId);
} 