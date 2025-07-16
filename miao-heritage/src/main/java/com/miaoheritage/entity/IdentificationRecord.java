package com.miaoheritage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 鉴别记录实体类
 * 用于记录用户提交的鉴别请求及结果
 */
@Entity
@Table(name = "identification_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @Column(name = "category_name")
    private String categoryName;
    
    @Column(name = "is_authentic")
    private Boolean isAuthentic;
    
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private Double confidenceScore;
    
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;
    
    @Column(name = "cultural_background", columnDefinition = "TEXT")
    private String culturalBackground;
    
    @OneToMany(mappedBy = "identificationRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IdentificationFeature> features = new ArrayList<>();
    
    @Column(name = "processing_status")
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * 处理状态枚举
     */
    public enum ProcessingStatus {
        PENDING,    // 等待处理
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        FAILED      // 处理失败
    }
} 