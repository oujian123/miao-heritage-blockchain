package com.miaoheritage.api.dto;

import com.miaoheritage.entity.Order;
import com.miaoheritage.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal actualPayment;
    private String paymentMethod;
    private String paymentId;
    private LocalDateTime paymentTime;
    private String shippingAddress;
    private String shippingName;
    private String shippingPhone;
    private LocalDateTime shippingTime;
    private String trackingNumber;
    private String transactionHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;
    private Long userId;
    private String username;
    
    /**
     * 从实体转换为DTO
     */
    public static OrderDTO fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setActualPayment(order.getActualPayment());
        
        if (order.getPaymentMethod() != null) {
            dto.setPaymentMethod(order.getPaymentMethod().name());
        }
        
        dto.setPaymentId(order.getPaymentId());
        dto.setPaymentTime(order.getPaymentTime());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingName(order.getShippingName());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setShippingTime(order.getShippingTime());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setTransactionHash(order.getTransactionHash());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // 处理订单项
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            dto.setItems(order.getItems().stream()
                    .map(OrderItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        // 处理用户信息
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
        }
        
        return dto;
    }
    
    /**
     * 订单项DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
        private String assetId;
        
        /**
         * 从实体转换为DTO
         */
        public static OrderItemDTO fromEntity(OrderItem orderItem) {
            if (orderItem == null) {
                return null;
            }
            
            return OrderItemDTO.builder()
                    .id(orderItem.getId())
                    .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
                    .productName(orderItem.getProductName())
                    .productImage(orderItem.getProductImage())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .subtotal(orderItem.getSubtotal())
                    .assetId(orderItem.getAssetId())
                    .build();
        }
    }
} 