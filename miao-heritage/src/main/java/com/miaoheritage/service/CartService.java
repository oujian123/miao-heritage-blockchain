package com.miaoheritage.service;

import com.miaoheritage.entity.CartItem;
import com.miaoheritage.entity.Product;
import com.miaoheritage.entity.User;
import com.miaoheritage.repository.CartItemRepository;
import com.miaoheritage.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AssetService assetService;
    
    /**
     * 获取用户购物车
     */
    public List<CartItem> getUserCart() {
        User currentUser = assetService.getCurrentUser();
        return cartItemRepository.findByUserId(currentUser.getId());
    }
    
    /**
     * 添加商品到购物车
     */
    @Transactional
    public CartItem addToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("商品数量必须大于0");
        }
        
        User currentUser = assetService.getCurrentUser();
        
        // 查找产品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        // 检查购物车中是否已存在该商品
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(currentUser.getId(), productId);
        
        if (existingItem.isPresent()) {
            // 更新数量
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartItemRepository.save(cartItem);
        } else {
            // 创建新购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUser(currentUser);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            return cartItemRepository.save(cartItem);
        }
    }
    
    /**
     * 更新购物车项数量
     */
    @Transactional
    public CartItem updateCartItemQuantity(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("商品数量必须大于0");
        }
        
        User currentUser = assetService.getCurrentUser();
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        
        // 检查是否是当前用户的购物车项
        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作该购物车项");
        }
        
        // 检查库存
        if (cartItem.getProduct().getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }
    
    /**
     * 从购物车移除商品
     */
    @Transactional
    public void removeFromCart(Long cartItemId) {
        User currentUser = assetService.getCurrentUser();
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        
        // 检查是否是当前用户的购物车项
        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权操作该购物车项");
        }
        
        cartItemRepository.delete(cartItem);
    }
    
    /**
     * 清空购物车
     */
    @Transactional
    public void clearCart() {
        User currentUser = assetService.getCurrentUser();
        cartItemRepository.deleteAllByUserId(currentUser.getId());
    }
    
    /**
     * 获取购物车中商品数量
     */
    public long getCartItemCount() {
        User currentUser = assetService.getCurrentUser();
        return cartItemRepository.countByUserId(currentUser.getId());
    }
} 