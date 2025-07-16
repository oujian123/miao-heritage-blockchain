package com.miaoheritage.service.impl;

import com.miaoheritage.entity.IdentificationFeature.FeatureType;
import com.miaoheritage.service.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 模拟AI模型服务实现
 * 用于本地开发和测试，不依赖外部API
 */
@Service
@Profile("dev")
@Slf4j
public class MockAiModelServiceImpl implements AiModelService {
    
    private final Random random = new Random();
    
    @Override
    public Map<String, Object> analyzeImage(MultipartFile imageFile, String categoryName) {
        log.info("模拟分析图像: {}, 类别: {}", imageFile.getOriginalFilename(), categoryName);
        
        // 提取特征
        List<Map<String, Object>> features = extractFeatures(imageFile, categoryName);
        
        // 判断真伪
        Map<String, Object> authenticityResult = determineAuthenticity(features, categoryName);
        
        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("isAuthentic", authenticityResult.get("isAuthentic"));
        result.put("confidenceScore", authenticityResult.get("confidenceScore"));
        result.put("analysis", generateMockAnalysis(
                (Boolean) authenticityResult.get("isAuthentic"), 
                (Double) authenticityResult.get("confidenceScore"), 
                categoryName
        ));
        result.put("features", features);
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> extractFeatures(MultipartFile imageFile, String categoryName) {
        log.info("模拟提取特征: {}, 类别: {}", imageFile.getOriginalFilename(), categoryName);
        
        List<Map<String, Object>> features = new ArrayList<>();
        
        // 根据类别生成不同的特征
        if ("银饰".equals(categoryName)) {
            features.add(createFeature(
                "银质纯度", 
                "银质纯度达到95%以上，呈现自然光泽",
                0.85, 
                true,
                "50,50,150,150",
                FeatureType.MATERIAL.name()
            ));
            
            features.add(createFeature(
                "传统纹饰", 
                "龙凤纹样设计精细，线条流畅，符合传统工艺特点",
                0.92, 
                true,
                "100,100,200,200",
                FeatureType.PATTERN.name()
            ));
            
            features.add(createFeature(
                "手工锤打痕迹", 
                "表面可见均匀细致的手工锤打痕迹，非机械加工",
                0.78, 
                true,
                "150,150,250,250",
                FeatureType.CRAFTSMANSHIP.name()
            ));
            
            // 随机添加一个可能的瑕疵点
            if (random.nextBoolean()) {
                features.add(createFeature(
                    "焊接痕迹", 
                    "部分连接处焊接痕迹明显，可能是后期修复",
                    0.65, 
                    false,
                    "200,200,250,250",
                    FeatureType.CRAFTSMANSHIP.name()
                ));
            }
        } else if ("服饰".equals(categoryName)) {
            features.add(createFeature(
                "刺绣工艺", 
                "刺绣细腻精致，针脚均匀，符合传统手工艺标准",
                0.88, 
                true,
                "50,50,150,150",
                FeatureType.CRAFTSMANSHIP.name()
            ));
            
            features.add(createFeature(
                "面料材质", 
                "使用传统手工织造棉麻材质，质地厚实",
                0.82, 
                true,
                "100,100,200,200",
                FeatureType.MATERIAL.name()
            ));
            
            features.add(createFeature(
                "传统图案", 
                "图案设计符合苗族传统审美，色彩鲜明协调",
                0.90, 
                true,
                "150,150,250,250",
                FeatureType.PATTERN.name()
            ));
            
            // 随机添加一个可能的瑕疵点
            if (random.nextBoolean()) {
                features.add(createFeature(
                    "染料特性", 
                    "部分区域染色不均匀，可能使用了现代化学染料",
                    0.70, 
                    false,
                    "200,200,250,250",
                    FeatureType.COLOR.name()
                ));
            }
        } else {
            // 默认特征
            features.add(createFeature(
                "材质特征", 
                "材质符合传统工艺要求",
                0.80, 
                true,
                "50,50,150,150",
                FeatureType.MATERIAL.name()
            ));
            
            features.add(createFeature(
                "工艺特征", 
                "工艺细节展现了传统制作手法",
                0.85, 
                true,
                "100,100,200,200",
                FeatureType.CRAFTSMANSHIP.name()
            ));
        }
        
        return features;
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
        log.info("模拟生成文化背景解读: {}, 特征数量: {}", categoryName, features.size());
        
        StringBuilder explanation = new StringBuilder();
        
        if ("银饰".equals(categoryName)) {
            explanation.append("苗族银饰是苗族文化的重要组成部分，具有悠久的历史和深厚的文化底蕴。\n\n");
            explanation.append("历史渊源：苗族银饰的历史可以追溯到数千年前，是苗族先民在长期迁徙过程中形成的独特文化表达。苗族人相信银具有辟邪驱鬼的神力，因此银饰不仅是装饰品，更是护身符。\n\n");
            explanation.append("工艺特点：传统苗族银饰采用纯银手工锻造，经过锤打、錾刻、焊接等工序精心制作。工艺师傅通常不使用图纸，全凭记忆和经验完成复杂的图案设计，体现了非物质文化遗产的珍贵价值。\n\n");
            explanation.append("文化象征：银饰图案多取材于自然和神话，如龙、凤、蝴蝶、鱼等，象征着苗族人民对自然的崇拜和对美好生活的向往。不同的图案和佩戴方式也反映了佩戴者的年龄、婚姻状况和社会地位。\n\n");
            explanation.append("地域特色：不同地区的苗族银饰有着明显的风格差异。贵州黔东南的银饰以精细繁复著称，湘西的银饰则更注重实用性，云南的苗族银饰融合了更多的傣族、彝族等民族元素。");
        } else if ("服饰".equals(categoryName)) {
            explanation.append("苗族服饰是中国最为绚丽多彩的民族服饰之一，被誉为"穿在身上的史诗"。\n\n");
            explanation.append("历史渊源：苗族服饰的历史可追溯到远古时期，是苗族人民在长期的历史发展中创造的文化瑰宝。服饰上的图案和纹样记录了苗族的迁徙历史、神话传说和生活习俗。\n\n");
            explanation.append("工艺特点：苗族服饰以精湛的刺绣、蜡染、挑花、织锦等工艺著称。一件精美的苗族盛装往往需要数月甚至数年时间完成，体现了苗族妇女的智慧和耐心。\n\n");
            explanation.append("文化象征：服饰上的图案多取材于自然和神话，如蝴蝶、鸟、龙等，象征着苗族人民对祖先的崇拜和对美好生活的向往。不同的服饰款式和图案也反映了穿着者的部落归属、年龄和婚姻状况。\n\n");
            explanation.append("地域特色：苗族服饰因地域不同而呈现出丰富多样的风格。贵州黔东南的"长裙苗"服饰色彩艳丽，湘西的"短裙苗"服饰简洁大方，云南的苗族服饰则融合了更多的傣族、彝族等民族元素。");
        } else {
            explanation.append("这件苗族文化遗产展现了苗族传统工艺和审美观念，是苗族文化的重要组成部分。\n\n");
            explanation.append("苗族是一个拥有悠久历史的民族，其文化遗产包含了丰富的历史信息和艺术价值。这件文物体现了苗族人民的智慧和创造力，是研究苗族历史文化的重要实物资料。");
        }
        
        return explanation.toString();
    }
    
    /**
     * 创建模拟特征
     */
    private Map<String, Object> createFeature(String name, String description, double confidence, 
                                             boolean isAuthenticIndicator, String location, String type) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("name", name);
        feature.put("description", description);
        feature.put("confidence", confidence);
        feature.put("isAuthenticIndicator", isAuthenticIndicator);
        feature.put("location", location);
        feature.put("type", type);
        return feature;
    }
    
    /**
     * 生成模拟分析报告
     */
    private String generateMockAnalysis(boolean isAuthentic, double confidenceScore, String categoryName) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("# 鉴别报告\n\n");
        analysis.append("## 总体评价\n\n");
        
        if (isAuthentic) {
            analysis.append("经过全面分析，该苗族").append(categoryName)
                   .append("被判定为真品，置信度为").append(String.format("%.2f", confidenceScore * 100)).append("%。\n\n");
            analysis.append("该物品展现了典型的苗族传统工艺特征，材质、纹饰和制作工艺均符合传统标准。");
        } else {
            analysis.append("经过全面分析，该苗族").append(categoryName)
                   .append("被判定为非真品，置信度为").append(String.format("%.2f", confidenceScore * 100)).append("%。\n\n");
            analysis.append("该物品虽然在外观上模仿了苗族传统风格，但在材质选用和工艺细节上存在明显现代化特征，不符合传统制作标准。");
        }
        
        analysis.append("\n\n## 特征分析\n\n");
        
        if ("银饰".equals(categoryName)) {
            if (isAuthentic) {
                analysis.append("1. **银质纯度**：银质纯度高，达到传统苗族银饰要求的95%以上，呈现自然光泽，无现代电镀痕迹。\n\n");
                analysis.append("2. **传统纹饰**：图案设计精细，龙凤纹样线条流畅自然，符合传统审美和象征意义。\n\n");
                analysis.append("3. **制作工艺**：表面可见均匀细致的手工锤打痕迹，非现代机械加工，体现了传统工艺的特点。");
            } else {
                analysis.append("1. **银质成分**：银质纯度较低，可能含有较高比例的其他金属，光泽不自然，有现代电镀痕迹。\n\n");
                analysis.append("2. **纹饰特点**：图案虽模仿传统，但线条缺乏手工制作的自然流畅感，过于规则，疑为机械压制。\n\n");
                analysis.append("3. **焊接痕迹**：部分连接处焊接痕迹明显，使用了现代焊接技术，不符合传统工艺特征。");
            }
        } else if ("服饰".equals(categoryName)) {
            if (isAuthentic) {
                analysis.append("1. **刺绣工艺**：刺绣细腻精致，针脚均匀，符合传统手工艺标准，展现了制作者的高超技艺。\n\n");
                analysis.append("2. **面料材质**：使用传统手工织造的棉麻材质，质地厚实，织法符合传统工艺特点。\n\n");
                analysis.append("3. **传统图案**：图案设计符合苗族传统审美，色彩鲜明协调，构图平衡，具有浓厚的民族特色。");
            } else {
                analysis.append("1. **刺绣特点**：刺绣虽精细，但针法过于规则，缺乏手工制作的自然变化，疑为机械刺绣。\n\n");
                analysis.append("2. **面料材质**：面料质地较轻薄，与传统手工织造的厚实质感不符，可能使用了现代工业织物。\n\n");
                analysis.append("3. **染料特性**：部分区域染色不均匀，色彩过于鲜艳，可能使用了现代化学染料，不符合传统植物染料特性。");
            }
        } else {
            if (isAuthentic) {
                analysis.append("1. **材质特征**：材质符合传统工艺要求，展现了自然的质感和历史痕迹。\n\n");
                analysis.append("2. **工艺特点**：工艺细节展现了传统制作手法，具有明显的手工特征。\n\n");
                analysis.append("3. **整体风格**：整体风格协调统一，符合历史时期的审美特点。");
            } else {
                analysis.append("1. **材质问题**：材质与传统工艺要求有明显差异，缺乏历史使用痕迹。\n\n");
                analysis.append("2. **工艺缺陷**：部分工艺细节不符合传统制作标准，可能使用了现代工具和技术。\n\n");
                analysis.append("3. **风格不协调**：整体风格存在不协调之处，混合了不同时期或地区的元素。");
            }
        }
        
        return analysis.toString();
    }
} 