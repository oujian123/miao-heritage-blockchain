package com.miaoheritage.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miaoheritage.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String assetId;
    private Long categoryId;
    private String categoryName;
    private List<ProductImageDTO> images;
    private String mainImage;
    private Double avgRating;
    private Integer reviewCount;
    private Set<String> tags;
    private String status;
    private String creatorName;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 区块链溯源信息
    private BlockchainInfoDTO blockchainInfo;
    
    /**
     * 从实体转换为DTO
     */
    public static ProductDTO fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setStock(product.getStock());
        dto.setAssetId(product.getAssetId());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        // 处理图片
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            dto.setImages(product.getImages().stream()
                    .map(ProductImageDTO::fromEntity)
                    .collect(Collectors.toList()));
            
            // 设置主图
            product.getImages().stream()
                    .filter(img -> img.isPrimary())
                    .findFirst()
                    .ifPresent(img -> dto.setMainImage(img.getUrl()));
        }
        
        dto.setAvgRating(product.getAvgRating());
        dto.setReviewCount(product.getReviewCount());
        
        // 处理标签
        if (product.getTags() != null && !product.getTags().isEmpty()) {
            dto.setTags(product.getTags().stream()
                    .map(tag -> tag.getName())
                    .collect(Collectors.toSet()));
        }
        
        dto.setStatus(product.getStatus().name());
        
        if (product.getCreator() != null) {
            dto.setCreatorId(product.getCreator().getId());
            dto.setCreatorName(product.getCreator().getUsername());
        }
        
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        return dto;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockchainInfoDTO {
        private String assetId;
        private String artisan;
        private String materialSource;
        private String owner;
        private List<HistoryDTO> history;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HistoryDTO {
            private LocalDateTime timestamp;
            private String operation;
            private String from;
            private String to;
            private String details;
        }
    }
} 