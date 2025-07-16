package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.OrderDTO;
import com.miaoheritage.entity.Order;
import com.miaoheritage.entity.Order.PaymentMethod;
import com.miaoheritage.service.OrderService;
import com.miaoheritage.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 * 处理支付相关的请求和回调
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    
    /**
     * 创建支付请求
     * 
     * @param orderId 订单ID
     * @return 支付参数，如支付链接、支付表单等
     */
    @PostMapping("/create/{orderId}")
    public ResponseEntity<Map<String, String>> createPayment(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 检查订单状态
            if (order.getStatus() != Order.OrderStatus.CREATED) {
                return ResponseEntity.badRequest().body(Map.of("error", "订单状态不正确，无法支付"));
            }
            
            // 创建支付
            Map<String, String> paymentResult = paymentService.createPayment(order, order.getPaymentMethod());
            return ResponseEntity.ok(paymentResult);
        } catch (Exception e) {
            log.error("创建支付请求失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "创建支付请求失败: " + e.getMessage()));
        }
    }
    
    /**
     * 查询支付状态
     * 
     * @param orderId 订单ID
     * @return 支付状态
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<Map<String, Object>> queryPaymentStatus(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            boolean isPaid = false;
            if (order.getStatus() == Order.OrderStatus.PAID || order.getStatus() == Order.OrderStatus.COMPLETED) {
                isPaid = true;
            } else if (order.getStatus() == Order.OrderStatus.CREATED && order.getPaymentId() != null) {
                // 查询支付状态
                isPaid = paymentService.queryPaymentStatus(order.getPaymentId(), order.getPaymentMethod());
                
                // 如果支付成功，更新订单状态
                if (isPaid) {
                    orderService.processPayment(order.getId(), order.getPaymentId());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("paid", isPaid);
            result.put("orderStatus", order.getStatus().name());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "查询支付状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 处理支付宝回调
     */
    @PostMapping("/notify/alipay")
    public void alipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processPaymentNotify(request, response, PaymentMethod.ALIPAY);
    }
    
    /**
     * 处理微信支付回调
     */
    @PostMapping("/notify/wechat")
    public void wechatPayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processPaymentNotify(request, response, PaymentMethod.WECHAT_PAY);
    }
    
    /**
     * 处理支付平台回调
     */
    private void processPaymentNotify(HttpServletRequest request, HttpServletResponse response, PaymentMethod method) 
            throws IOException {
        Map<String, String> params = new HashMap<>();
        
        // 获取请求参数
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            params.put(name, request.getParameter(name));
        }
        
        // 如果是微信支付，需要处理JSON请求体
        if (method == PaymentMethod.WECHAT_PAY) {
            String requestBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            params.put("body", requestBody);
        }
        
        log.info("收到支付回调通知: method={}, params={}", method, params);
        
        boolean success = false;
        try {
            // 验证支付回调
            success = paymentService.handlePaymentCallback(params, method);
            
            if (success) {
                // 获取订单信息
                String outTradeNo = params.get(method == PaymentMethod.ALIPAY ? "out_trade_no" : "out_trade_no");
                String tradeNo = params.get(method == PaymentMethod.ALIPAY ? "trade_no" : "transaction_id");
                
                // 更新订单状态
                Order order = orderService.getOrderByNumber(outTradeNo);
                if (order != null) {
                    orderService.processPayment(order.getId(), tradeNo);
                } else {
                    log.error("未找到对应的订单: {}", outTradeNo);
                    success = false;
                }
            }
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            success = false;
        }
        
        // 返回结果给支付平台
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        if (method == PaymentMethod.ALIPAY) {
            writer.write(success ? "success" : "fail");
        } else if (method == PaymentMethod.WECHAT_PAY) {
            writer.write("{\"code\":\"" + (success ? "SUCCESS" : "FAIL") + "\",\"message\":\"" +
                    (success ? "成功" : "失败") + "\"}");
        }
        writer.flush();
    }
    
    /**
     * 支付完成前端回调
     */
    @GetMapping("/return")
    public ResponseEntity<Map<String, String>> paymentReturn(@RequestParam Map<String, String> params) {
        log.info("支付完成前端回调: params={}", params);
        
        String orderId = params.get("out_trade_no");
        if (orderId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "订单号不能为空"));
        }
        
        return ResponseEntity.ok(Map.of("orderId", orderId, "status", "success"));
    }
    
    /**
     * 退款请求
     */
    @PostMapping("/refund/{orderId}")
    public ResponseEntity<Map<String, String>> refund(
            @PathVariable Long orderId,
            @RequestParam(required = false) BigDecimal amount) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (order.getStatus() != Order.OrderStatus.PAID && 
                order.getStatus() != Order.OrderStatus.PROCESSING) {
                return ResponseEntity.badRequest().body(Map.of("error", "订单状态不支持退款"));
            }
            
            if (order.getPaymentId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "订单未支付，无法退款"));
            }
            
            // 如果未指定退款金额，则全额退款
            BigDecimal refundAmount = (amount != null) ? amount : order.getActualPayment();
            
            // 处理退款
            Map<String, String> result = paymentService.refund(order.getPaymentId(), refundAmount, order.getPaymentMethod());
            
            if (Boolean.parseBoolean(result.get("success"))) {
                // 更新订单状态为退款中
                orderService.updateOrderStatus(orderId, Order.OrderStatus.REFUNDING);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("处理退款请求失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "处理退款请求失败: " + e.getMessage()));
        }
    }
} 