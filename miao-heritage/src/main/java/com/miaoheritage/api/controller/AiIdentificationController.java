package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.IdentificationRecordDTO;
import com.miaoheritage.api.dto.IdentificationRequest;
import com.miaoheritage.entity.IdentificationRecord;
import com.miaoheritage.service.AiIdentificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * AI鉴别控制器
 * 处理文化遗产鉴别和文化背景解读请求
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiIdentificationController {
    
    private final AiIdentificationService aiIdentificationService;
    
    /**
     * 提交鉴别请求
     * 
     * @param imageFile 待鉴别图像
     * @param categoryName 类别名称
     * @return 鉴别记录
     */
    @PostMapping(value = "/identify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IdentificationRecordDTO> submitIdentification(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("categoryName") String categoryName) {
        
        try {
            log.info("接收到鉴别请求: 文件名={}, 类别={}", imageFile.getOriginalFilename(), categoryName);
            
            IdentificationRecord record = aiIdentificationService.submitIdentification(imageFile, categoryName);
            return ResponseEntity.ok(IdentificationRecordDTO.fromEntity(record));
        } catch (Exception e) {
            log.error("鉴别请求处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取鉴别记录
     * 
     * @param recordId 记录ID
     * @return 鉴别记录详情
     */
    @GetMapping("/records/{recordId}")
    public ResponseEntity<IdentificationRecordDTO> getIdentificationRecord(@PathVariable Long recordId) {
        try {
            IdentificationRecord record = aiIdentificationService.getIdentificationRecord(recordId);
            return ResponseEntity.ok(IdentificationRecordDTO.fromEntity(record));
        } catch (Exception e) {
            log.error("获取鉴别记录失败", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * 获取当前用户的鉴别记录列表
     * 
     * @return 鉴别记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<List<IdentificationRecordDTO>> getCurrentUserRecords() {
        try {
            List<IdentificationRecord> records = aiIdentificationService.getCurrentUserIdentificationRecords();
            return ResponseEntity.ok(IdentificationRecordDTO.fromEntities(records));
        } catch (Exception e) {
            log.error("获取用户鉴别记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取文化背景解读
     * 
     * @param categoryName 类别名称
     * @param features 特征列表
     * @return 文化背景解读
     */
    @PostMapping("/cultural-background")
    public ResponseEntity<Map<String, String>> getCulturalBackground(
            @RequestParam String categoryName,
            @RequestBody List<String> features) {
        
        try {
            String culturalBackground = aiIdentificationService.generateCulturalBackground(categoryName, features);
            return ResponseEntity.ok(Map.of("culturalBackground", culturalBackground));
        } catch (Exception e) {
            log.error("获取文化背景解读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 