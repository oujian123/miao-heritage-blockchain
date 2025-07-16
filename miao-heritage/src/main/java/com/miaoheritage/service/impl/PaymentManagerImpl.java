package com.miaoheritage.service.impl;

import com.miaoheritage.entity.Order;
import com.miaoheritage.entity.Order.PaymentMethod;
import com.miaoheritage.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * 支付管理服务
 * 负责根据支付方式选择对应的支付实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentManagerImpl implements PaymentService {
    
    private final AlipayServiceImpl alipayService;
    private final WechatPayServiceImpl wechatPayService;
    
    /**
     * 根据支付方式获取对应的支付服务实现
     */
    private PaymentService getServiceByMethod(PaymentMethod method) {
        switch (method) {
            case ALIPAY:
                return alipayService;
            case WECHAT_PAY:
                return wechatPayService;
            default:
                throw new IllegalArgumentException("不支持的支付方式: " + method);
        }
    }
    
    @Override
    public Map<String, String> createPayment(Order order, PaymentMethod method) {
        return getServiceByMethod(method).createPayment(order, method);
    }
    
    @Override
    public boolean queryPaymentStatus(String paymentId, PaymentMethod method) {
        return getServiceByMethod(method).queryPaymentStatus(paymentId, method);
    }
    
    @Override
    public boolean handlePaymentCallback(Map<String, String> params, PaymentMethod method) {
        return getServiceByMethod(method).handlePaymentCallback(params, method);
    }
    
    @Override
    public Map<String, String> refund(String paymentId, BigDecimal refundAmount, PaymentMethod method) {
        return getServiceByMethod(method).refund(paymentId, refundAmount, method);
    }
    
    @Override
    protected boolean verifySignature(Map<String, String> params) {
        // 该方法不应被直接调用
        throw new UnsupportedOperationException("支付管理服务不直接处理签名验证");
    }
    
    @Override
    protected String buildSignature(Map<String, String> params) {
        // 该方法不应被直接调用
        throw new UnsupportedOperationException("支付管理服务不直接处理签名构建");
    }
} 