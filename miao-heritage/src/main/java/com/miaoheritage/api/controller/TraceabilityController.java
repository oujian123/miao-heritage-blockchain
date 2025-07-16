package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.AssetResponseDTO;
import com.miaoheritage.api.dto.CreateAssetRequest;
import com.miaoheritage.blockchain.model.AssetDTO;
import com.miaoheritage.blockchain.service.BlockchainService;
import com.miaoheritage.service.AssetService;
import com.miaoheritage.service.QRCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/v1/trace")
@RequiredArgsConstructor
@Slf4j
public class TraceabilityController {

    private final BlockchainService blockchainService;
    private final AssetService assetService;
    private final QRCodeService qrCodeService;
    
    /**
     * 创建新资产
     */
    @PostMapping("/assets")
    @PreAuthorize("hasRole('ARTISAN') or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<AssetResponseDTO>> createAsset(
            @RequestBody @Valid CreateAssetRequest request) {
        
        log.info("接收到创建资产请求: {}", request);
        
        // 1. 生成资产唯一ID
        String assetId = UUID.randomUUID().toString();
        
        // 2. 构建资产DTO
        AssetDTO assetDTO = AssetDTO.builder()
                .id(assetId)
                .type(request.getType())
                .name(request.getName())
                .description(request.getDescription())
                .owner(assetService.getCurrentUserIdentifier()) // 初始所有者为创建者
                .artisan(request.getArtisan())
                .artisanId(request.getArtisanId())
                .materialSource(request.getMaterialSource())
                .attributes(request.getAttributes())
                .build();
        
        // 3. 保存到区块链
        return blockchainService.createAsset(assetDTO)
                .thenCompose(id -> assetService.saveAssetMetadata(assetDTO))
                .thenCompose(id -> blockchainService.queryAsset(id))
                .thenApply(asset -> {
                    // 4. 构建响应
                    AssetResponseDTO response = new AssetResponseDTO();
                    response.setId(asset.getId());
                    response.setType(asset.getType());
                    response.setName(asset.getName());
                    response.setDescription(asset.getDescription());
                    response.setOwner(asset.getOwner());
                    response.setArtisan(asset.getArtisan());
                    response.setMaterialSource(asset.getMaterialSource());
                    response.setCreateTimestamp(asset.getCreateTime());
                    response.setHistory(asset.getHistory());
                    response.setAttributes(asset.getAttributes());
                    
                    // 设置二维码URL
                    response.setQrCodeUrl("/v1/trace/qrcode/" + asset.getId());
                    
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(ex -> {
                    log.error("创建资产失败", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }
    
    /**
     * 获取资产信息
     */
    @GetMapping("/assets/{id}")
    public CompletableFuture<ResponseEntity<AssetResponseDTO>> getAsset(@PathVariable String id) {
        return blockchainService.queryAsset(id)
                .thenCompose(asset -> assetService.enrichAssetWithMetadata(asset))
                .thenApply(enrichedAsset -> {
                    AssetResponseDTO response = new AssetResponseDTO();
                    response.setId(enrichedAsset.getId());
                    response.setType(enrichedAsset.getType());
                    response.setName(enrichedAsset.getName());
                    response.setDescription(enrichedAsset.getDescription());
                    response.setOwner(enrichedAsset.getOwner());
                    response.setArtisan(enrichedAsset.getArtisan());
                    response.setMaterialSource(enrichedAsset.getMaterialSource());
                    response.setCreateTimestamp(enrichedAsset.getCreateTime());
                    response.setHistory(enrichedAsset.getHistory());
                    response.setAttributes(enrichedAsset.getAttributes());
                    
                    // 设置二维码和图片URL
                    response.setQrCodeUrl("/v1/trace/qrcode/" + enrichedAsset.getId());
                    response.setImageUrl("/v1/assets/images/" + enrichedAsset.getId());
                    
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    log.error("查询资产失败: {}", id, ex);
                    return ResponseEntity.notFound().build();
                });
    }
    
    /**
     * 生成资产溯源二维码
     */
    @GetMapping(value = "/qrcode/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public CompletableFuture<ResponseEntity<byte[]>> getQRCode(@PathVariable String id) {
        try {
            byte[] qrCodeImage = qrCodeService.generateQRCode(id);
            return CompletableFuture.completedFuture(
                    ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG)
                            .body(qrCodeImage)
            );
        } catch (Exception e) {
            log.error("生成二维码失败: {}", id, e);
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }
    
    /**
     * 转移资产所有权
     */
    @PutMapping("/assets/{id}/owner")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<Void>> transferAsset(
            @PathVariable String id, @RequestParam String newOwner) {
        
        return blockchainService.transferAsset(id, newOwner)
                .thenApply(result -> ResponseEntity.ok().<Void>build())
                .exceptionally(ex -> {
                    log.error("转移资产所有权失败: {}", id, ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }
} 