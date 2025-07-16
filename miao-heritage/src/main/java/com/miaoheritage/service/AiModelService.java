package com.miaoheritage.service;

import com.miaoheritage.entity.IdentificationFeature;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * AI模型服务接口
 * 负责与外部AI模型交互，处理图像识别和文本生成
 */
public interface AiModelService {
    
    /**
     * 使用计算机视觉模型分析图像
     * 
     * @param imageFile 图像文件
     * @param categoryName 类别名称
     * @return 分析结果，包含是否真品、可信度、特征列表等
     */
    Map<String, Object> analyzeImage(MultipartFile imageFile, String categoryName);
    
    /**
     * 使用大语言模型生成文化背景解读
     * 
     * @param categoryName 类别名称
     * @param features 识别到的特征列表
     * @return 文化背景解读文本
     */
    String generateCulturalExplanation(String categoryName, List<String> features);
    
    /**
     * 提取图像中的特征点
     * 
     * @param imageFile 图像文件
     * @param categoryName 类别名称
     * @return 特征列表
     */
    List<Map<String, Object>> extractFeatures(MultipartFile imageFile, String categoryName);
    
    /**
     * 判断图像是否为真品
     * 
     * @param features 特征列表
     * @param categoryName 类别名称
     * @return 鉴别结果，包含真伪判断和可信度
     */
    Map<String, Object> determineAuthenticity(List<Map<String, Object>> features, String categoryName);
} 