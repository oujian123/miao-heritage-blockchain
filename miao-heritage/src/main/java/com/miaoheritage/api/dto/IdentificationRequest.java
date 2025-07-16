package com.miaoheritage.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationRequest {
    
    @NotBlank(message = "类别名称不能为空")
    private String categoryName;
} 