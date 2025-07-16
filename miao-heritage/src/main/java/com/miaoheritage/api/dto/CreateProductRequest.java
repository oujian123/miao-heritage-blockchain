package com.miaoheritage.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class CreateProductRequest {
    
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 100, message = "商品名称长度必须在2-100之间")
    private String name;
    
    @Size(max = 4000, message = "商品描述长度不能超过4000个字符")
    private String description;
    
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格不能小于0")
    private BigDecimal price;
    
    @Min(value = 0, message = "商品原价不能小于0")
    private BigDecimal originalPrice;
    
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;
    
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;
    
    private String status;
    
    private String assetId; // 区块链资产ID，可选
    
    private Set<String> tags;
} 