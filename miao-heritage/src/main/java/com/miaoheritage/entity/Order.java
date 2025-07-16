package com.miaoheritage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;
    
    @Column(name = "actual_payment", precision = 10, scale = 2)
    private BigDecimal actualPayment;
    
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
    
    @Column(name = "shipping_address", length = 500, nullable = false)
    private String shippingAddress;
    
    @Column(name = "shipping_name", nullable = false)
    private String shippingName;
    
    @Column(name = "shipping_phone", nullable = false)
    private String shippingPhone;
    
    @Column(name = "shipping_time")
    private LocalDateTime shippingTime;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "transaction_hash")
    private String transactionHash; // 区块链交易哈希
    
    @Column
    private String remark;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum OrderStatus {
        CREATED,       // 已创建
        PAID,          // 已支付
        PROCESSING,    // 处理中
        SHIPPED,       // 已发货
        DELIVERED,     // 已送达
        COMPLETED,     // 已完成
        CANCELLED,     // 已取消
        REFUNDING,     // 退款中
        REFUNDED       // 已退款
    }
    
    public enum PaymentMethod {
        ALIPAY,      // 支付宝
        WECHAT_PAY,  // 微信支付
        UNION_PAY,   // 银联支付
        CREDIT_CARD, // 信用卡
        WALLET       // 平台钱包
    }
} 