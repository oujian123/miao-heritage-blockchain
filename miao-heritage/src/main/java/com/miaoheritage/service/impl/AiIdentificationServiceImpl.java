package com.miaoheritage.service.impl;

import com.miaoheritage.entity.IdentificationFeature;
import com.miaoheritage.entity.IdentificationRecord;
import com.miaoheritage.entity.IdentificationRecord.ProcessingStatus;
import com.miaoheritage.entity.User;
import com.miaoheritage.repository.IdentificationFeatureRepository;
import com.miaoheritage.repository.IdentificationRecordRepository;
import com.miaoheritage.service.AiIdentificationService;
import com.miaoheritage.service.AiModelService;
import com.miaoheritage.service.AssetService;
import com.miaoheritage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiIdentificationServiceImpl implements AiIdentificationService {
    
    private final IdentificationRecordRepository identificationRecordRepository;
    private final IdentificationFeatureRepository identificationFeatureRepository;
    private final AiModelService aiModelService;
    private final FileStorageService fileStorageService;
    private final AssetService assetService;
    
    @Override
    @Transactional
    public IdentificationRecord submitIdentification(MultipartFile imageFile, String categoryName) {
        try {
            // 获取当前用户
            User currentUser = assetService.getCurrentUser();
            
            // 保存图片
            String imageUrl = fileStorageService.storeFile(imageFile);
            
            // 创建鉴别记录
            IdentificationRecord record = IdentificationRecord.builder()
                    .user(currentUser)
                    .imageUrl(imageUrl)
                    .categoryName(categoryName)
                    .status(ProcessingStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // 保存记录
            IdentificationRecord savedRecord = identificationRecordRepository.save(record);
            
            // 异步处理鉴别请求
            processIdentification(savedRecord.getId());
            
            return savedRecord;
        } catch (Exception e) {
            log.error("提交鉴别请求失败", e);
            throw new RuntimeException("提交鉴别请求失败: " + e.getMessage());
        }
    }
    
    @Override
    @Async
    @Transactional
    public CompletableFuture<IdentificationRecord> processIdentification(Long recordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取鉴别记录
                IdentificationRecord record = identificationRecordRepository.findById(recordId)
                        .orElseThrow(() -> new RuntimeException("鉴别记录不存在"));
                
                // 更新状态为处理中
                record.setStatus(ProcessingStatus.PROCESSING);
                identificationRecordRepository.save(record);
                
                // 获取图像文件
                MultipartFile imageFile = fileStorageService.getFileAsMultipartFile(record.getImageUrl());
                
                // 调用AI模型分析图像
                Map<String, Object> analysisResult = aiModelService.analyzeImage(imageFile, record.getCategoryName());
                
                // 提取分析结果
                Boolean isAuthentic = (Boolean) analysisResult.get("isAuthentic");
                Double confidenceScore = (Double) analysisResult.get("confidenceScore");
                String aiAnalysis = (String) analysisResult.get("analysis");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> featuresData = (List<Map<String, Object>>) analysisResult.get("features");
                
                // 更新记录
                record.setIsAuthentic(isAuthentic);
                record.setConfidenceScore(confidenceScore);
                record.setAiAnalysis(aiAnalysis);
                record.setStatus(ProcessingStatus.COMPLETED);
                record.setCompletedAt(LocalDateTime.now());
                
                // 保存特征
                List<IdentificationFeature> features = new ArrayList<>();
                for (Map<String, Object> featureData : featuresData) {
                    IdentificationFeature feature = IdentificationFeature.builder()
                            .identificationRecord(record)
                            .featureName((String) featureData.get("name"))
                            .featureDescription((String) featureData.get("description"))
                            .confidenceScore((Double) featureData.get("confidence"))
                            .isAuthenticIndicator((Boolean) featureData.get("isAuthenticIndicator"))
                            .featureLocation((String) featureData.get("location"))
                            .featureType(IdentificationFeature.FeatureType.valueOf((String) featureData.get("type")))
                            .build();
                    
                    features.add(feature);
                }
                
                // 生成文化背景解读
                List<String> featureNames = features.stream()
                        .map(IdentificationFeature::getFeatureName)
                        .collect(Collectors.toList());
                
                String culturalBackground = generateCulturalBackground(record.getCategoryName(), featureNames);
                record.setCulturalBackground(culturalBackground);
                
                // 保存记录和特征
                IdentificationRecord savedRecord = identificationRecordRepository.save(record);
                identificationFeatureRepository.saveAll(features);
                
                return savedRecord;
            } catch (Exception e) {
                log.error("处理鉴别请求失败", e);
                
                // 更新状态为失败
                IdentificationRecord record = identificationRecordRepository.findById(recordId).orElse(null);
                if (record != null) {
                    record.setStatus(ProcessingStatus.FAILED);
                    record.setAiAnalysis("处理失败: " + e.getMessage());
                    identificationRecordRepository.save(record);
                }
                
                throw new RuntimeException("处理鉴别请求失败: " + e.getMessage());
            }
        });
    }
    
    @Override
    public IdentificationRecord getIdentificationRecord(Long recordId) {
        return identificationRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("鉴别记录不存在"));
    }
    
    @Override
    public List<IdentificationRecord> getCurrentUserIdentificationRecords() {
        User currentUser = assetService.getCurrentUser();
        return identificationRecordRepository.findByUserId(currentUser.getId());
    }
    
    @Override
    public String generateCulturalBackground(String categoryName, List<String> features) {
        return aiModelService.generateCulturalExplanation(categoryName, features);
    }
} 