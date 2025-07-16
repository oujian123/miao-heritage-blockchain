package com.miaoheritage.api.dto;

import com.miaoheritage.entity.IdentificationFeature;
import com.miaoheritage.entity.IdentificationRecord;
import com.miaoheritage.entity.IdentificationRecord.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationRecordDTO {
    
    private Long id;
    private String imageUrl;
    private String categoryName;
    private Boolean isAuthentic;
    private Double confidenceScore;
    private String aiAnalysis;
    private String culturalBackground;
    private List<IdentificationFeatureDTO> features;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String username;
    
    /**
     * 从实体转换为DTO
     */
    public static IdentificationRecordDTO fromEntity(IdentificationRecord record) {
        List<IdentificationFeatureDTO> featureDTOs = record.getFeatures().stream()
                .map(IdentificationFeatureDTO::fromEntity)
                .collect(Collectors.toList());
        
        return IdentificationRecordDTO.builder()
                .id(record.getId())
                .imageUrl(record.getImageUrl())
                .categoryName(record.getCategoryName())
                .isAuthentic(record.getIsAuthentic())
                .confidenceScore(record.getConfidenceScore())
                .aiAnalysis(record.getAiAnalysis())
                .culturalBackground(record.getCulturalBackground())
                .features(featureDTOs)
                .status(record.getStatus() != null ? record.getStatus().name() : null)
                .createdAt(record.getCreatedAt())
                .completedAt(record.getCompletedAt())
                .username(record.getUser() != null ? record.getUser().getUsername() : null)
                .build();
    }
    
    /**
     * 从实体列表转换为DTO列表
     */
    public static List<IdentificationRecordDTO> fromEntities(List<IdentificationRecord> records) {
        return records.stream()
                .map(IdentificationRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 