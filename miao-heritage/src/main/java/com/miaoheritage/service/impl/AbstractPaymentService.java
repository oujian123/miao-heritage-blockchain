package com.miaoheritage.service.impl;

import com.miaoheritage.entity.Order;
import com.miaoheritage.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象支付服务基类
 * 提供通用功能和实现
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentService implements PaymentService {
    
    @Value("${payment.return-url:http://localhost:8080/api/payment/return}")
    protected String returnUrl;
    
    @Value("${payment.notify-url:http://localhost:8080/api/payment/notify}")
    protected String notifyUrl;
    
    /**
     * 生成商户订单号
     * 基于当前时间和订单ID生成唯一的商户订单号
     */
    protected String generateMerchantOrderNo(Order order) {
        // 使用时间戳和订单ID生成唯一订单号
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timeStr = LocalDateTime.now().format(formatter);
        return String.format("MH%s%06d", timeStr, order.getId());
    }
    
    /**
     * 构建基础支付参数
     */
    protected Map<String, String> buildBaseParams(Order order) {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", generateMerchantOrderNo(order));
        params.put("subject", "苗族文化遗产商品订单");
        params.put("totalAmount", order.getActualPayment().toString());
        params.put("returnUrl", returnUrl);
        params.put("notifyUrl", notifyUrl);
        return params;
    }
    
    /**
     * 签名验证接口
     * 验证支付回调签名的合法性
     * 
     * @param params 回调参数
     * @return 验证结果，true为验证通过
     */
    protected abstract boolean verifySignature(Map<String, String> params);
    
    /**
     * 构建API请求签名
     * 
     * @param params 请求参数
     * @return 签名
     */
    protected abstract String buildSignature(Map<String, String> params);
} 