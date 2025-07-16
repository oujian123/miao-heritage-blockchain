package com.miaoheritage.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.miaoheritage.entity.Order;
import com.miaoheritage.entity.Order.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AlipayServiceImpl extends AbstractPaymentService {

    @Value("${payment.alipay.app-id}")
    private String appId;
    
    @Value("${payment.alipay.private-key}")
    private String privateKey;
    
    @Value("${payment.alipay.public-key}")
    private String publicKey;
    
    @Value("${payment.alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String gatewayUrl;
    
    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
            gatewayUrl,
            appId,
            privateKey,
            "json",
            "UTF-8",
            publicKey,
            "RSA2"
        );
    }
    
    @Override
    public Map<String, String> createPayment(Order order, PaymentMethod method) {
        if (method != PaymentMethod.ALIPAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        Map<String, String> result = new HashMap<>();
        Map<String, String> baseParams = buildBaseParams(order);
        
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(returnUrl);
            request.setNotifyUrl(notifyUrl);
            
            // 构建请求业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", baseParams.get("outTradeNo"));
            bizContent.put("total_amount", baseParams.get("totalAmount"));
            bizContent.put("subject", baseParams.get("subject"));
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            
            // 将业务参数转为JSON
            request.setBizContent(mapToJsonString(bizContent));
            
            // 发送请求并获取支付表单
            String form = alipayClient.pageExecute(request).getBody();
            result.put("form", form);
            result.put("paymentType", "form");
            
            return result;
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付请求失败", e);
            throw new RuntimeException("创建支付请求失败", e);
        }
    }
    
    @Override
    public boolean queryPaymentStatus(String paymentId, PaymentMethod method) {
        if (method != PaymentMethod.ALIPAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            
            // 设置业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", paymentId);
            request.setBizContent(mapToJsonString(bizContent));
            
            // 查询支付状态
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                // 支付成功的状态: TRADE_SUCCESS 或 TRADE_FINISHED
                String tradeStatus = response.getTradeStatus();
                return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            } else {
                log.warn("支付宝查询支付状态失败: {}, {}", response.getCode(), response.getMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("查询支付宝支付状态失败", e);
            return false;
        }
    }
    
    @Override
    public boolean handlePaymentCallback(Map<String, String> params, PaymentMethod method) {
        if (method != PaymentMethod.ALIPAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        // 验证签名
        if (!verifySignature(params)) {
            log.warn("支付宝回调签名验证失败");
            return false;
        }
        
        // 验证支付状态
        String tradeStatus = params.get("trade_status");
        return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
    }
    
    @Override
    public Map<String, String> refund(String paymentId, BigDecimal refundAmount, PaymentMethod method) {
        if (method != PaymentMethod.ALIPAY) {
            throw new IllegalArgumentException("支付方式不匹配");
        }
        
        Map<String, String> result = new HashMap<>();
        
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            
            // 设置业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", paymentId);
            bizContent.put("refund_amount", refundAmount.toString());
            bizContent.put("refund_reason", "苗族文化遗产商品退款");
            
            request.setBizContent(mapToJsonString(bizContent));
            
            // 执行退款
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                result.put("success", "true");
                result.put("refundId", response.getOutTradeNo());
            } else {
                result.put("success", "false");
                result.put("errorCode", response.getCode());
                result.put("errorMsg", response.getMsg());
            }
            
            return result;
        } catch (AlipayApiException e) {
            log.error("支付宝退款处理失败", e);
            result.put("success", "false");
            result.put("errorMsg", e.getMessage());
            return result;
        }
    }
    
    @Override
    protected boolean verifySignature(Map<String, String> params) {
        try {
            // 移除sign和sign_type参数
            String sign = params.get("sign");
            Map<String, String> paramsToVerify = new HashMap<>(params);
            paramsToVerify.remove("sign");
            paramsToVerify.remove("sign_type");
            
            // 验证签名
            return AlipaySignature.rsaVerifyV2(paramsToVerify, publicKey, "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            log.error("支付宝签名验证失败", e);
            return false;
        }
    }
    
    @Override
    protected String buildSignature(Map<String, String> params) {
        // 支付宝SDK已经封装了签名实现，这里不需要单独实现
        return "";
    }
    
    /**
     * 将Map转换为JSON字符串
     */
    private String mapToJsonString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":\"")
              .append(entry.getValue()).append("\",");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }
} 