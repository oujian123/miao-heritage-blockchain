package com.miaoheritage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoheritage.entity.IdentificationFeature.FeatureType;
import com.miaoheritage.service.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * DeepSeek API实现的AI模型服务
 * 使用DeepSeek的计算机视觉和大语言模型API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeepSeekAiModelServiceImpl implements AiModelService {
    
    @Value("${ai.deepseek.api-key}")
    private String apiKey;
    
    @Value("${ai.deepseek.vision-api-url}")
    private String visionApiUrl;
    
    @Value("${ai.deepseek.llm-api-url}")
    private String llmApiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public Map<String, Object> analyzeImage(MultipartFile imageFile, String categoryName) {
        try {
            // 提取特征
            List<Map<String, Object>> features = extractFeatures(imageFile, categoryName);
            
            // 判断真伪
            Map<String, Object> authenticityResult = determineAuthenticity(features, categoryName);
            
            // 生成分析报告
            String analysis = generateAnalysisReport(features, authenticityResult, categoryName);
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("isAuthentic", authenticityResult.get("isAuthentic"));
            result.put("confidenceScore", authenticityResult.get("confidenceScore"));
            result.put("analysis", analysis);
            result.put("features", features);
            
            return result;
        } catch (Exception e) {
            log.error("图像分析失败", e);
            throw new RuntimeException("图像分析失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> extractFeatures(MultipartFile imageFile, String categoryName) {
        try {
            // 准备请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 构建请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("image", resource);
            body.add("prompt", generateFeatureExtractionPrompt(categoryName));
            body.add("response_format", "json");
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    visionApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            // 解析响应
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode featuresNode = jsonResponse.path("features");
                
                List<Map<String, Object>> features = new ArrayList<>();
                
                if (featuresNode.isArray()) {
                    for (JsonNode featureNode : featuresNode) {
                        Map<String, Object> feature = new HashMap<>();
                        feature.put("name", featureNode.path("name").asText());
                        feature.put("description", featureNode.path("description").asText());
                        feature.put("confidence", featureNode.path("confidence").asDouble());
                        feature.put("isAuthenticIndicator", featureNode.path("is_authentic_indicator").asBoolean());
                        feature.put("location", featureNode.path("location").asText());
                        feature.put("type", featureNode.path("type").asText());
                        
                        features.add(feature);
                    }
                }
                
                return features;
            } else {
                log.error("特征提取API调用失败: {}", response.getBody());
                throw new RuntimeException("特征提取失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("特征提取失败", e);
            throw new RuntimeException("特征提取失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> determineAuthenticity(List<Map<String, Object>> features, String categoryName) {
        // 计算真伪得分
        double authenticScore = 0.0;
        double totalWeight = 0.0;
        
        for (Map<String, Object> feature : features) {
            Boolean isAuthenticIndicator = (Boolean) feature.get("isAuthenticIndicator");
            Double confidence = (Double) feature.get("confidence");
            
            if (isAuthenticIndicator != null && confidence != null) {
                double weight = confidence;
                authenticScore += isAuthenticIndicator ? weight : -weight;
                totalWeight += weight;
            }
        }
        
        // 归一化得分到0-1范围
        double normalizedScore = (authenticScore + totalWeight) / (2 * totalWeight);
        
        // 判断真伪
        boolean isAuthentic = normalizedScore > 0.7; // 阈值可调整
        
        Map<String, Object> result = new HashMap<>();
        result.put("isAuthentic", isAuthentic);
        result.put("confidenceScore", normalizedScore);
        
        return result;
    }
    
    @Override
    public String generateCulturalExplanation(String categoryName, List<String> features) {
        try {
            // 准备请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "你是一位苗族文化专家，精通苗族银饰、服饰等文化遗产的历史、工艺和文化意义。请根据用户提供的特征，详细解读其文化背景和意义。"));
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("请为一件苗族").append(categoryName).append("提供详细的文化背景解读。\n");
            prompt.append("该物品具有以下特征：\n");
            
            for (String feature : features) {
                prompt.append("- ").append(feature).append("\n");
            }
            
            prompt.append("\n请从历史渊源、工艺特点、文化象征意义、地域特色等方面进行解读，帮助人们理解这件苗族文化遗产的价值。");
            
            messages.add(Map.of("role", "user", "content", prompt.toString()));
            requestBody.put("messages", messages);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    llmApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            // 解析响应
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.path("choices").get(0).path("message").path("content").asText();
            } else {
                log.error("文化解读API调用失败: {}", response.getBody());
                return "无法获取文化背景解读信息。";
            }
        } catch (Exception e) {
            log.error("生成文化背景解读失败", e);
            return "生成文化背景解读时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 生成特征提取的提示词
     */
    private String generateFeatureExtractionPrompt(String categoryName) {
        return "请分析这张苗族" + categoryName + "的图片，识别其中的关键特征，并以JSON格式返回结果。" +
                "每个特征需包含以下字段：name（特征名称）、description（特征描述）、confidence（置信度，0-1之间）、" +
                "is_authentic_indicator（是否为真品指标，true或false）、location（位置，如'x1,y1,x2,y2'）、" +
                "type（特征类型，可选值：PATTERN, MATERIAL, CRAFTSMANSHIP, COLOR, STRUCTURE, OTHER）。" +
                "请特别关注工艺细节、材质特征、纹样特点等可能区分真伪的关键点。";
    }
    
    /**
     * 生成分析报告
     */
    private String generateAnalysisReport(List<Map<String, Object>> features, Map<String, Object> authenticityResult, String categoryName) {
        try {
            // 准备请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "你是一位苗族文化遗产鉴别专家，精通苗族银饰、服饰等文化遗产的真伪鉴别。"));
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("请根据以下特征分析，生成一份关于这件苗族").append(categoryName).append("的鉴别报告：\n\n");
            prompt.append("特征列表：\n");
            
            for (Map<String, Object> feature : features) {
                prompt.append("- ").append(feature.get("name")).append("：").append(feature.get("description"));
                prompt.append("（置信度：").append(feature.get("confidence")).append("）\n");
            }
            
            prompt.append("\n鉴别结果：");
            prompt.append(authenticityResult.get("isAuthentic").equals(true) ? "真品" : "非真品");
            prompt.append("（置信度：").append(authenticityResult.get("confidenceScore")).append("）\n\n");
            
            prompt.append("请提供一份专业、客观的分析报告，解释为什么判断其为真品或非真品，并指出关键的鉴别特征。报告应包含总体评价和具体特征分析两部分。");
            
            messages.add(Map.of("role", "user", "content", prompt.toString()));
            requestBody.put("messages", messages);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    llmApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            
            // 解析响应
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.path("choices").get(0).path("message").path("content").asText();
            } else {
                log.error("分析报告API调用失败: {}", response.getBody());
                return "无法生成分析报告。";
            }
        } catch (Exception e) {
            log.error("生成分析报告失败", e);
            return "生成分析报告时发生错误: " + e.getMessage();
        }
    }
} 