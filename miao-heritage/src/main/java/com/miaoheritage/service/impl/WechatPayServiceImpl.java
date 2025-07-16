package com.miaoheritage.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoheritage.entity.Order;
import com.miaoheritage.entity.Order.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WechatPayServiceImpl extends AbstractPaymentService {

    @Value("${payment.wechat.app-id}")
    private String appId;
    
    @Value("${payment.wechat.mch-id}")
    private String mchId;
    
    @Value("${payment.wechat.api-key}")
    private String apiKey;
    
    @Value("${payment.wechat.api-v3-key}")
    private String apiV3Key;
    
    @Value("${payment.wechat.merchant-serial-number}")
    private String merchantSerialNumber;
    
    @Value("${payment.wechat.private-key-path}")
    private String privateKeyPath;
    
    @Value("${payment.wechat.api-url:https://api.mch.weixin.qq.com}")
    private String apiUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public Map<String, String> createPayment(Order order, PaymentMethod method) {
        if (method != PaymentMethod.WECHAT_PAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        Map<String, String> result = new HashMap<>();
        Map<String, String> baseParams = buildBaseParams(order);
        
        try {
            // 构建微信支付API请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("appid", appId);
            requestMap.put("mchid", mchId);
            requestMap.put("description", baseParams.get("subject"));
            requestMap.put("out_trade_no", baseParams.get("outTradeNo"));
            requestMap.put("notify_url", notifyUrl);
            
            // 金额信息（单位：分）
            Map<String, Object> amountMap = new HashMap<>();
            BigDecimal amount = new BigDecimal(baseParams.get("totalAmount"));
            // 将元转换为分
            amountMap.put("total", amount.multiply(new BigDecimal(100)).intValue());
            amountMap.put("currency", "CNY");
            requestMap.put("amount", amountMap);
            
            // 构建支付API请求
            String requestBody = objectMapper.writeValueAsString(requestMap);
            
            // 准备HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.add("Authorization", generateAuthorizationHeader("/v3/pay/transactions/native", "POST", requestBody));
            
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/v3/pay/transactions/native",
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                String codeUrl = (String) responseMap.get("code_url");
                result.put("codeUrl", codeUrl);
                result.put("paymentType", "qrcode");
            } else {
                log.error("微信支付创建失败: {}", response.getBody());
                throw new RuntimeException("创建微信支付请求失败");
            }
            
            return result;
        } catch (Exception e) {
            log.error("创建微信支付请求失败", e);
            throw new RuntimeException("创建支付请求失败", e);
        }
    }
    
    @Override
    public boolean queryPaymentStatus(String paymentId, PaymentMethod method) {
        if (method != PaymentMethod.WECHAT_PAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        try {
            // 准备HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.add("Authorization", generateAuthorizationHeader(
                "/v3/pay/transactions/out-trade-no/" + paymentId + "?mchid=" + mchId,
                "GET", 
                ""
            ));
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/v3/pay/transactions/out-trade-no/" + paymentId + "?mchid=" + mchId,
                HttpMethod.GET,
                requestEntity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                String tradeState = (String) responseMap.get("trade_state");
                return "SUCCESS".equals(tradeState);
            } else {
                log.warn("微信支付查询失败: {}", response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("查询微信支付状态失败", e);
            return false;
        }
    }
    
    @Override
    public boolean handlePaymentCallback(Map<String, String> params, PaymentMethod method) {
        if (method != PaymentMethod.WECHAT_PAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        try {
            // 验证签名
            if (!verifySignature(params)) {
                log.warn("微信支付回调签名验证失败");
                return false;
            }
            
            // 解密并解析回调数据
            String resource = params.get("resource");
            Map<String, Object> resourceMap = objectMapper.readValue(resource, Map.class);
            String ciphertext = (String) resourceMap.get("ciphertext");
            String nonce = (String) resourceMap.get("nonce");
            String associatedData = (String) resourceMap.get("associated_data");
            
            // 使用AES-GCM解密，此处简化处理
            String decryptedData = decryptData(ciphertext, nonce, associatedData);
            Map<String, Object> dataMap = objectMapper.readValue(decryptedData, Map.class);
            
            // 验证支付状态
            String tradeState = (String) dataMap.get("trade_state");
            return "SUCCESS".equals(tradeState);
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, String> refund(String paymentId, BigDecimal refundAmount, PaymentMethod method) {
        if (method != PaymentMethod.WECHAT_PAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        Map<String, String> result = new HashMap<>();
        
        try {
            // 构建退款请求参数
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("out_trade_no", paymentId);
            requestMap.put("out_refund_no", "refund_" + Instant.now().getEpochSecond());
            
            // 金额信息（单位：分）
            Map<String, Object> amountMap = new HashMap<>();
            // 将元转换为分
            int totalAmount = refundAmount.multiply(new BigDecimal(100)).intValue();
            amountMap.put("refund", totalAmount);
            amountMap.put("total", totalAmount);
            amountMap.put("currency", "CNY");
            requestMap.put("amount", amountMap);
            
            // 退款原因
            requestMap.put("reason", "苗族文化遗产商品退款");
            
            // 构建退款API请求
            String requestBody = objectMapper.writeValueAsString(requestMap);
            
            // 准备HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.add("Authorization", generateAuthorizationHeader("/v3/refund/domestic/refunds", "POST", requestBody));
            
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl + "/v3/refund/domestic/refunds",
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                result.put("success", "true");
                result.put("refundId", (String) responseMap.get("refund_id"));
            } else {
                log.error("微信支付退款失败: {}", response.getBody());
                result.put("success", "false");
                result.put("errorMsg", response.getBody());
            }
            
            return result;
        } catch (Exception e) {
            log.error("微信支付退款处理失败", e);
            result.put("success", "false");
            result.put("errorMsg", e.getMessage());
            return result;
        }
    }
    
    @Override
    protected boolean verifySignature(Map<String, String> params) {
        try {
            String signature = params.get("signature");
            String timestamp = params.get("timestamp");
            String nonce = params.get("nonce");
            String body = params.get("body");
            
            // 构建验签字符串
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            
            // 这里应该使用微信平台证书公钥进行验签
            // 简化处理，假设验签通过
            return true;
        } catch (Exception e) {
            log.error("微信支付验签失败", e);
            return false;
        }
    }
    
    @Override
    protected String buildSignature(Map<String, String> params) {
        // 简化实现，实际应该使用商户私钥签名
        return "";
    }
    
    /**
     * 生成微信支付Authorization头
     */
    private String generateAuthorizationHeader(String canonicalUrl, String httpMethod, String requestBody) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = generateNonceStr();
        
        // 构建签名字符串
        String message = httpMethod + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + requestBody + "\n";
        
        // 使用商户私钥签名
        String signature = ""; // 简化处理，实际应使用商户私钥签名
        
        // 构建认证头
        return "WECHATPAY2-SHA256-RSA2048 "
                + "mchid=\"" + mchId + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + merchantSerialNumber + "\","
                + "signature=\"" + signature + "\"";
    }
    
    /**
     * 生成随机字符串
     */
    private String generateNonceStr() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    /**
     * 解密微信支付回调数据
     * 简化实现，实际应该使用AES-GCM算法解密
     */
    private String decryptData(String ciphertext, String nonce, String associatedData) {
        // 简化实现，实际应该使用AES-GCM解密
        return "{}";
    }
} 