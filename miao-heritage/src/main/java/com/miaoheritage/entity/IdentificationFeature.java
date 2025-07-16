package com.miaoheritage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 鉴别特征实体类
 * 记录AI识别的具体特征点及其可信度
 */
@Entity
@Table(name = "identification_features")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationFeature {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "record_id", nullable = false)
    private IdentificationRecord identificationRecord;
    
    @Column(name = "feature_name", nullable = false)
    private String featureName;
    
    @Column(name = "feature_description", columnDefinition = "TEXT")
    private String featureDescription;
    
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private Double confidenceScore;
    
    @Column(name = "is_authentic_indicator")
    private Boolean isAuthenticIndicator;
    
    @Column(name = "feature_location")
    private String featureLocation; // 图像中的位置坐标，如 "x1,y1,x2,y2"
    
    @Column(name = "feature_type")
    @Enumerated(EnumType.STRING)
    private FeatureType featureType;
    
    /**
     * 特征类型枚举
     */
    public enum FeatureType {
        PATTERN,      // 图案特征
        MATERIAL,     // 材质特征
        CRAFTSMANSHIP, // 工艺特征
        COLOR,        // 颜色特征
        STRUCTURE,    // 结构特征
        OTHER         // 其他特征
    }
} 