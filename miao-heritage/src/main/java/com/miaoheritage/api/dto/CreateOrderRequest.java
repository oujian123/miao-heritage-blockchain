package com.miaoheritage.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderRequest {
    
    @NotBlank(message = "收货地址不能为空")
    private String shippingAddress;
    
    @NotBlank(message = "收货人不能为空")
    private String shippingName;
    
    @NotBlank(message = "收货电话不能为空")
    private String shippingPhone;
    
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    private String remark;
} 