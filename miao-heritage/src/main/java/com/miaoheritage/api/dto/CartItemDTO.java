package com.miaoheritage.api.dto;

import com.miaoheritage.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    
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
    public static CartItemDTO fromEntity(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getProduct().getPrice());
        dto.setSubtotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        dto.setAssetId(cartItem.getProduct().getAssetId());
        
        // 设置商品主图
        if (cartItem.getProduct().getImages() != null && !cartItem.getProduct().getImages().isEmpty()) {
            cartItem.getProduct().getImages().stream()
                    .filter(img -> img.isPrimary())
                    .findFirst()
                    .ifPresent(img -> dto.setProductImage(img.getUrl()));
        }
        
        return dto;
    }
} 