package com.miaoheritage.service;

import com.miaoheritage.entity.IdentificationRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI鉴别服务接口
 * 提供苗族文化遗产真伪鉴别和文化解读功能
 */
public interface AiIdentificationService {
    
    /**
     * 提交鉴别请求
     * 
     * @param imageFile 待鉴别图像
     * @param categoryName 类别名称（如"银饰"、"服饰"等）
     * @return 鉴别记录
     */
    IdentificationRecord submitIdentification(MultipartFile imageFile, String categoryName);
    
    /**
     * 异步处理鉴别请求
     * 
     * @param recordId 鉴别记录ID
     * @return 异步处理结果
     */
    CompletableFuture<IdentificationRecord> processIdentification(Long recordId);
    
    /**
     * 获取鉴别记录
     * 
     * @param recordId 记录ID
     * @return 鉴别记录
     */
    IdentificationRecord getIdentificationRecord(Long recordId);
    
    /**
     * 获取当前用户的鉴别记录列表
     * 
     * @return 鉴别记录列表
     */
    List<IdentificationRecord> getCurrentUserIdentificationRecords();
    
    /**
     * 获取文化背景解读
     * 
     * @param categoryName 类别名称
     * @param features 识别到的特征列表
     * @return 文化背景解读文本
     */
    String generateCulturalBackground(String categoryName, List<String> features);
} 