package com.miaoheritage.service;

import com.miaoheritage.entity.Order;
import com.miaoheritage.entity.Order.PaymentMethod;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付服务接口
 * 处理各种支付方式的支付请求
 */
public interface PaymentService {
    
    /**
     * 创建支付请求
     * 
     * @param order 需要支付的订单
     * @param method 支付方式
     * @return 支付参数，包含支付链接、表单等
     */
    Map<String, String> createPayment(Order order, PaymentMethod method);
    
    /**
     * 查询支付状态
     * 
     * @param paymentId 支付ID
     * @param method 支付方式
     * @return 支付状态，true为成功，false为失败或处理中
     */
    boolean queryPaymentStatus(String paymentId, PaymentMethod method);
    
    /**
     * 处理支付回调通知
     * 
     * @param params 回调参数
     * @param method 支付方式
     * @return 处理结果
     */
    boolean handlePaymentCallback(Map<String, String> params, PaymentMethod method);
    
    /**
     * 退款处理
     * 
     * @param paymentId 原支付ID
     * @param refundAmount 退款金额
     * @param method 原支付方式
     * @return 退款结果，包含退款ID等信息
     */
    Map<String, String> refund(String paymentId, BigDecimal refundAmount, PaymentMethod method);
} 