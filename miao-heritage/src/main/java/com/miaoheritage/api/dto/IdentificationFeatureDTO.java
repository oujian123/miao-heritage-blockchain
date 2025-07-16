package com.miaoheritage.api.dto;

import com.miaoheritage.entity.IdentificationFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationFeatureDTO {
    
    private Long id;
    private String featureName;
    private String featureDescription;
    private Double confidenceScore;
    private Boolean isAuthenticIndicator;
    private String featureLocation;
    private String featureType;
    
    /**
     * 从实体转换为DTO
     */
    public static IdentificationFeatureDTO fromEntity(IdentificationFeature feature) {
        return IdentificationFeatureDTO.builder()
                .id(feature.getId())
                .featureName(feature.getFeatureName())
                .featureDescription(feature.getFeatureDescription())
                .confidenceScore(feature.getConfidenceScore())
                .isAuthenticIndicator(feature.getIsAuthenticIndicator())
                .featureLocation(feature.getFeatureLocation())
                .featureType(feature.getFeatureType() != null ? feature.getFeatureType().name() : null)
                .build();
    }
} 