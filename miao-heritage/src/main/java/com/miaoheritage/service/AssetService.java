package com.miaoheritage.service;

import com.miaoheritage.blockchain.model.AssetDTO;
import com.miaoheritage.entity.User;
import com.miaoheritage.repository.UserRepository;
import com.miaoheritage.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final UserService userService;

    /**
     * 获取当前用户标识符
     */
    public String getCurrentUserIdentifier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            if (auth.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                return userDetails.getUsername();
            }
            return auth.getName();
        }
        return "anonymous";
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            if (auth.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                return userService.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("找不到当前用户"));
            }
        }
        throw new RuntimeException("用户未登录");
    }
    
    /**
     * 保存资产元数据到数据库
     * 这里将链下数据与链上数据进行关联
     */
    public CompletableFuture<String> saveAssetMetadata(AssetDTO assetDTO) {
        // TODO: 将链下数据保存到数据库，如图片URL、详细描述等
        
        // 模拟异步保存操作
        return CompletableFuture.completedFuture(assetDTO.getId());
    }
    
    /**
     * 用链下数据丰富资产信息
     */
    public CompletableFuture<AssetDTO> enrichAssetWithMetadata(AssetDTO assetDTO) {
        // TODO: 从数据库中查询链下数据，并将其添加到assetDTO中
        
        // 模拟异步查询操作
        return CompletableFuture.completedFuture(assetDTO);
    }
    
    /**
     * 获取资产完整详情
     */
    public AssetDTO getAssetFullDetail(String assetId) {
        // TODO: 实现完整的资产详情查询，包含链上和链下数据
        
        // 临时返回空对象，实际项目中需实现
        return AssetDTO.builder().id(assetId).build();
    }
} 