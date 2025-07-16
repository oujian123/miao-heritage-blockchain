package com.miaoheritage.service.impl;

import com.miaoheritage.blockchain.service.BlockchainService;
import com.miaoheritage.entity.*;
import com.miaoheritage.entity.Order.OrderStatus;
import com.miaoheritage.entity.Order.PaymentMethod;
import com.miaoheritage.repository.OrderRepository;
import com.miaoheritage.repository.ProductRepository;
import com.miaoheritage.service.AssetService;
import com.miaoheritage.service.CartService;
import com.miaoheritage.service.OrderService;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final AssetService assetService;
    private final BlockchainService blockchainService;
    
    @Override
    public Page<Order> getCurrentUserOrders(Pageable pageable) {
        User currentUser = assetService.getCurrentUser();
        return orderRepository.findByUserId(currentUser.getId(), pageable);
    }
    
    @Override
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    @Override
    public Page<Order> getCurrentUserOrdersByStatus(OrderStatus status, Pageable pageable) {
        User currentUser = assetService.getCurrentUser();
        return orderRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
    }
    
    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }
    
    @Override
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElse(null);
    }
    
    @Override
    @Transactional
    public Order createOrderFromCart(String shippingAddress, String shippingName, 
                                    String shippingPhone, PaymentMethod paymentMethod) {
        User currentUser = assetService.getCurrentUser();
        List<CartItem> cartItems = cartService.getUserCart();
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(currentUser);
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(shippingAddress);
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setPaymentMethod(paymentMethod);
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 处理购物车项目
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // 检查库存
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("商品 " + product.getName() + " 库存不足");
            }
            
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            
            // 设置主图
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                product.getImages().stream()
                        .filter(ProductImage::isPrimary)
                        .findFirst()
                        .ifPresent(image -> orderItem.setProductImage(image.getUrl()));
            }
            
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItem.setAssetId(product.getAssetId()); // 关联区块链资产ID
            
            orderItems.add(orderItem);
            
            // 累计总金额
            totalAmount = totalAmount.add(orderItem.getSubtotal());
            
            // 减少库存
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        // 设置订单信息
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setActualPayment(totalAmount); // 可以在此添加折扣、运费等逻辑
        order.setCreatedAt(LocalDateTime.now());
        
        // 保存订单
        Order savedOrder = orderRepository.save(order);
        
        // 清空购物车
        cartService.clearCart();
        
        return savedOrder;
    }
    
    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 检查状态流转是否合法
        validateStatusChange(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        
        // 根据状态设置相应的时间
        switch (newStatus) {
            case PAID:
                order.setPaymentTime(LocalDateTime.now());
                break;
            case SHIPPED:
                order.setShippingTime(LocalDateTime.now());
                break;
            default:
                // 其他状态不需要特殊处理
                break;
        }
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public Order processPayment(Long orderId, String paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("订单状态不正确");
        }
        
        // 设置支付信息
        order.setPaymentId(paymentId);
        order.setPaymentTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PAID);
        
        // 处理区块链资产转移
        processBlockchainAssetTransfer(order);
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 只有未支付的订单可以取消
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("只有未支付的订单可以取消");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        
        // 恢复库存
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * 处理区块链资产转移
     */
    private void processBlockchainAssetTransfer(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getAssetId() != null && !item.getAssetId().isEmpty()) {
                try {
                    // 转移资产所有权
                    CompletableFuture<String> future = blockchainService.transferAsset(
                            item.getAssetId(), 
                            order.getUser().getBlockchainIdentity());
                    
                    future.thenAccept(transactionId -> {
                        // 记录交易哈希
                        order.setTransactionHash(transactionId);
                        orderRepository.save(order);
                    }).exceptionally(ex -> {
                        log.error("区块链资产转移失败: {}", item.getAssetId(), ex);
                        return null;
                    });
                } catch (Exception e) {
                    log.error("调用区块链服务失败: {}", item.getAssetId(), e);
                }
            }
        }
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "M" + timestamp + uuid;
    }
    
    /**
     * 验证订单状态变更是否合法
     */
    private void validateStatusChange(OrderStatus currentStatus, OrderStatus newStatus) {
        // 实际项目中应该有更复杂的状态流转逻辑
        boolean isValid = false;
        
        switch (currentStatus) {
            case CREATED:
                isValid = newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
                break;
            case PAID:
                isValid = newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.REFUNDING;
                break;
            case PROCESSING:
                isValid = newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.REFUNDING;
                break;
            case SHIPPED:
                isValid = newStatus == OrderStatus.DELIVERED;
                break;
            case DELIVERED:
                isValid = newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.REFUNDING;
                break;
            default:
                isValid = false;
        }
        
        if (!isValid) {
            throw new IllegalStateException("非法的订单状态变更: " + currentStatus + " -> " + newStatus);
        }
    }
} 