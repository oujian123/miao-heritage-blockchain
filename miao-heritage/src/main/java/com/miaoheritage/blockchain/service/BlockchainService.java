package com.miaoheritage.blockchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoheritage.blockchain.config.FabricConfig;
import com.miaoheritage.blockchain.model.AssetDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainService {

    private final FabricConfig fabricConfig;
    private final ObjectMapper objectMapper;
    
    private Gateway gateway;
    private Contract contract;
    
    @PostConstruct
    public void init() throws IOException {
        // 加载网络配置
        Path networkConfigPath = Paths.get(fabricConfig.getNetworkConfigPath());
        Path walletPath = Paths.get(fabricConfig.getWalletDirectory());
        
        // 加载身份钱包
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        
        // 构建网关连接配置
        Gateway.Builder builder = Gateway.createBuilder()
                .identity(wallet, fabricConfig.getUserName())
                .networkConfig(networkConfigPath)
                .discovery(true);
        
        // 创建网关连接
        this.gateway = builder.connect();
        
        // 获取通道和合约
        Network network = gateway.getNetwork(fabricConfig.getChannelName());
        this.contract = network.getContract(fabricConfig.getChaincodeName());
        
        log.info("区块链服务初始化成功，连接到通道: {}, 智能合约: {}", 
                fabricConfig.getChannelName(), fabricConfig.getChaincodeName());
    }
    
    /**
     * 创建新资产
     */
    public CompletableFuture<String> createAsset(AssetDTO assetDTO) {
        try {
            // 序列化扩展属性
            String attributesJson = assetDTO.getAttributes() != null ? 
                    objectMapper.writeValueAsString(assetDTO.getAttributes()) : "";
            
            // 调用智能合约
            return contract.submitAsync("CreateAsset",
                    assetDTO.getId(),
                    assetDTO.getType(),
                    assetDTO.getName(),
                    assetDTO.getDescription(),
                    assetDTO.getOwner(),
                    assetDTO.getArtisan(),
                    assetDTO.getArtisanId(),
                    assetDTO.getMaterialSource(),
                    assetDTO.getCertHash(),
                    assetDTO.getImageHash(),
                    attributesJson)
                    .thenApply(result -> assetDTO.getId());
        } catch (JsonProcessingException e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("序列化资产属性失败", e));
            return future;
        }
    }
    
    /**
     * 查询资产
     */
    public CompletableFuture<AssetDTO> queryAsset(String assetId) {
        return contract.evaluateAsync("QueryAsset", assetId)
                .thenApply(result -> {
                    try {
                        return objectMapper.readValue(new String(result, StandardCharsets.UTF_8), AssetDTO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("资产数据解析失败", e);
                    }
                });
    }
    
    /**
     * 转移资产
     */
    public CompletableFuture<String> transferAsset(String assetId, String newOwner) {
        return contract.submitAsync("TransferAsset", assetId, newOwner)
                .thenApply(result -> assetId);
    }
    
    /**
     * 资产认证
     */
    public CompletableFuture<String> certifyAsset(String assetId, String certifier, String certHash, String details) {
        return contract.submitAsync("CertifyAsset", assetId, certifier, certHash, details)
                .thenApply(result -> assetId);
    }
    
    /**
     * 添加资产历史记录
     */
    public CompletableFuture<String> addHistory(String assetId, String operation, 
                                             String from, String to, String details) {
        return contract.submitAsync("AddHistory", assetId, operation, from, to, details)
                .thenApply(result -> assetId);
    }
    
    /**
     * 更新资产属性
     */
    public CompletableFuture<String> updateAttributes(String assetId, Map<String, Object> attributes) {
        try {
            String attributesJson = objectMapper.writeValueAsString(attributes);
            return contract.submitAsync("UpdateAttributes", assetId, attributesJson)
                    .thenApply(result -> assetId);
        } catch (JsonProcessingException e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("序列化资产属性失败", e));
            return future;
        }
    }
} 