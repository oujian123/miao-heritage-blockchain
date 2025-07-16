package com.miaoheritage.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssetRequest {
    
    @NotBlank(message = "资产类型不能为空")
    private String type;           // 类型：银饰/服饰
    
    @NotBlank(message = "资产名称不能为空")
    private String name;           // 名称
    
    private String description;    // 描述
    
    @NotBlank(message = "制作匠人不能为空")
    private String artisan;        // 制作匠人
    
    private String artisanId;      // 匠人身份识别
    
    @NotBlank(message = "材料来源不能为空")
    private String materialSource; // 材料来源
    
    private Map<String, Object> attributes; // 扩展属性
} 