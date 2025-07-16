package com.miaoheritage.api.dto;

import com.miaoheritage.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    
    private Long id;
    private String url;
    private Integer sortOrder;
    private boolean isPrimary;
    private String description;
    
    /**
     * 从实体转换为DTO
     */
    public static ProductImageDTO fromEntity(ProductImage image) {
        if (image == null) {
            return null;
        }
        
        return ProductImageDTO.builder()
                .id(image.getId())
                .url(image.getUrl())
                .sortOrder(image.getSortOrder())
                .isPrimary(image.isPrimary())
                .description(image.getDescription())
                .build();
    }
} 