package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.CreateProductRequest;
import com.miaoheritage.api.dto.ProductDTO;
import com.miaoheritage.blockchain.model.AssetDTO;
import com.miaoheritage.blockchain.service.BlockchainService;
import com.miaoheritage.entity.Category;
import com.miaoheritage.entity.Product;
import com.miaoheritage.entity.Product.Status;
import com.miaoheritage.entity.Tag;
import com.miaoheritage.service.CategoryService;
import com.miaoheritage.service.ProductService;
import com.miaoheritage.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final BlockchainService blockchainService;
    
    /**
     * 获取商品列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage;
        
        if (status != null && categoryId != null) {
            Status productStatus = Status.valueOf(status);
            productPage = productService.findByStatusAndCategoryId(productStatus, categoryId, pageable);
        } else if (status != null) {
            Status productStatus = Status.valueOf(status);
            productPage = productService.findByStatus(productStatus, pageable);
        } else if (categoryId != null) {
            productPage = productService.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productService.findByStatus(Status.PUBLISHED, pageable);
        }
        
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productDTOs);
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.search(query, pageable);
        
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productDTOs);
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductDetail(@PathVariable Long id) {
        return productService.findById(id)
                .map(product -> {
                    ProductDTO productDTO = ProductDTO.fromEntity(product);
                    
                    // 如果有区块链资产ID，则查询区块链信息
                    if (product.getAssetId() != null && !product.getAssetId().isEmpty()) {
                        try {
                            blockchainService.queryAsset(product.getAssetId())
                                    .thenAccept(assetDTO -> enrichProductWithBlockchainInfo(productDTO, assetDTO));
                        } catch (Exception e) {
                            log.error("查询区块链资产失败", e);
                        }
                    }
                    
                    return ResponseEntity.ok(productDTO);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建商品
     */
    @PostMapping
    @PreAuthorize("hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(
            @RequestPart("product") @Valid CreateProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        
        // 检查分类是否存在
        Category category = categoryService.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        // 创建商品实体
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setAssetId(request.getAssetId());
        
        // 设置状态
        if (request.getStatus() != null) {
            try {
                product.setStatus(Status.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                product.setStatus(Status.DRAFT);
            }
        } else {
            product.setStatus(Status.DRAFT);
        }
        
        // 处理标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagService.createIfNotExists(tagName);
                tags.add(tag);
            }
            product.setTags(tags);
        }
        
        // 创建商品
        Product savedProduct = productService.createProduct(product, images);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductDTO.fromEntity(savedProduct));
    }
    
    /**
     * 更新商品信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid CreateProductRequest request) {
        
        return productService.findById(id)
                .map(product -> {
                    // 更新基本信息
                    product.setName(request.getName());
                    product.setDescription(request.getDescription());
                    product.setPrice(request.getPrice());
                    product.setOriginalPrice(request.getOriginalPrice());
                    product.setStock(request.getStock());
                    
                    // 更新分类
                    if (request.getCategoryId() != null) {
                        categoryService.findById(request.getCategoryId())
                                .ifPresent(product::setCategory);
                    }
                    
                    // 更新状态
                    if (request.getStatus() != null) {
                        try {
                            product.setStatus(Status.valueOf(request.getStatus()));
                        } catch (IllegalArgumentException e) {
                            // 保持原状态不变
                        }
                    }
                    
                    // 更新资产ID
                    if (request.getAssetId() != null) {
                        product.setAssetId(request.getAssetId());
                    }
                    
                    // 更新标签
                    if (request.getTags() != null) {
                        Set<Tag> tags = new HashSet<>();
                        for (String tagName : request.getTags()) {
                            Tag tag = tagService.createIfNotExists(tagName);
                            tags.add(tag);
                        }
                        product.setTags(tags);
                    }
                    
                    // 保存更新
                    Product updatedProduct = productService.updateProduct(product);
                    return ResponseEntity.ok(ProductDTO.fromEntity(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 更新商品状态
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        try {
            Status productStatus = Status.valueOf(status);
            Product updatedProduct = productService.updateProductStatus(id, productStatus);
            return ResponseEntity.ok(ProductDTO.fromEntity(updatedProduct));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 关联区块链资产ID到商品
     */
    @PostMapping("/{id}/asset")
    @PreAuthorize("hasRole('ARTISAN') or hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> linkAssetToProduct(
            @PathVariable Long id,
            @RequestParam String assetId) {
        
        try {
            Product updatedProduct = productService.linkAssetToProduct(id, assetId);
            return ResponseEntity.ok(ProductDTO.fromEntity(updatedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 用区块链资产信息丰富商品DTO
     */
    private void enrichProductWithBlockchainInfo(ProductDTO productDTO, AssetDTO assetDTO) {
        if (assetDTO == null) return;
        
        ProductDTO.BlockchainInfoDTO blockchainInfo = new ProductDTO.BlockchainInfoDTO();
        blockchainInfo.setAssetId(assetDTO.getId());
        blockchainInfo.setArtisan(assetDTO.getArtisan());
        blockchainInfo.setMaterialSource(assetDTO.getMaterialSource());
        blockchainInfo.setOwner(assetDTO.getOwner());
        
        if (assetDTO.getHistory() != null && !assetDTO.getHistory().isEmpty()) {
            List<ProductDTO.BlockchainInfoDTO.HistoryDTO> historyList = new ArrayList<>();
            
            assetDTO.getHistory().forEach(history -> {
                ProductDTO.BlockchainInfoDTO.HistoryDTO historyDTO = new ProductDTO.BlockchainInfoDTO.HistoryDTO();
                historyDTO.setTimestamp(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(history.getTimestamp()),
                        ZoneId.systemDefault()));
                historyDTO.setOperation(history.getOperation());
                historyDTO.setFrom(history.getFrom());
                historyDTO.setTo(history.getTo());
                historyDTO.setDetails(history.getDetails());
                
                historyList.add(historyDTO);
            });
            
            blockchainInfo.setHistory(historyList);
        }
        
        productDTO.setBlockchainInfo(blockchainInfo);
    }
} 