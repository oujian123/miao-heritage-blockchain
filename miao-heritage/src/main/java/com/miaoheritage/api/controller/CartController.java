package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.CartItemDTO;
import com.miaoheritage.api.dto.MessageResponse;
import com.miaoheritage.entity.CartItem;
import com.miaoheritage.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    /**
     * 获取当前用户购物车
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserCart() {
        List<CartItem> cartItems = cartService.getUserCart();
        
        List<CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(CartItemDTO::fromEntity)
                .collect(Collectors.toList());
        
        // 计算总价
        double totalPrice = cartItemDTOs.stream()
                .mapToDouble(item -> item.getSubtotal().doubleValue())
                .sum();
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", cartItemDTOs);
        response.put("totalPrice", totalPrice);
        response.put("totalItems", cartItems.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<CartItemDTO> addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        
        CartItem cartItem = cartService.addToCart(productId, quantity);
        return ResponseEntity.ok(CartItemDTO.fromEntity(cartItem));
    }
    
    /**
     * 更新购物车项数量
     */
    @PutMapping("/{cartItemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<CartItemDTO> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        
        CartItem cartItem = cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(CartItemDTO.fromEntity(cartItem));
    }
    
    /**
     * 从购物车移除商品
     */
    @DeleteMapping("/{cartItemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(new MessageResponse("商品已从购物车移除"));
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(new MessageResponse("购物车已清空"));
    }
    
    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('USER') or hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getCartItemCount() {
        long count = cartService.getCartItemCount();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
} 