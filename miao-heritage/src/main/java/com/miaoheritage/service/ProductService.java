package com.miaoheritage.service;

import com.miaoheritage.blockchain.service.BlockchainService;
import com.miaoheritage.entity.Product;
import com.miaoheritage.entity.Product.Status;
import com.miaoheritage.entity.ProductImage;
import com.miaoheritage.entity.User;
import com.miaoheritage.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final BlockchainService blockchainService;
    private final AssetService assetService;
    private final FileStorageService fileStorageService;
    
    /**
     * 获取商品详情
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * 通过区块链资产ID获取商品
     */
    public Optional<Product> findByAssetId(String assetId) {
        return productRepository.findByAssetId(assetId);
    }
    
    /**
     * 创建商品
     */
    @Transactional
    public Product createProduct(Product product, List<MultipartFile> images) {
        // 设置默认状态
        if (product.getStatus() == null) {
            product.setStatus(Status.DRAFT);
        }
        
        // 设置默认评分数据
        product.setAvgRating(0.0);
        product.setReviewCount(0);
        
        // 设置创建者
        product.setCreator(assetService.getCurrentUser());
        
        // 先保存商品，获取ID
        Product savedProduct = productRepository.save(product);
        
        // 处理商品图片
        if (images != null && !images.isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            int sortOrder = 0;
            
            for (MultipartFile file : images) {
                try {
                    String imageUrl = fileStorageService.storeProductImage(file, savedProduct.getId());
                    
                    ProductImage image = new ProductImage();
                    image.setProduct(savedProduct);
                    image.setUrl(imageUrl);
                    image.setSortOrder(sortOrder++);
                    image.setPrimary(sortOrder == 1); // 第一张图片设为主图
                    
                    productImages.add(image);
                } catch (Exception e) {
                    log.error("存储商品图片失败", e);
                    // 继续处理下一张图片
                }
            }
            
            savedProduct.setImages(productImages);
            savedProduct = productRepository.save(savedProduct);
        }
        
        return savedProduct;
    }
    
    /**
     * 更新商品信息
     */
    @Transactional
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }
    
    /**
     * 更新商品状态
     */
    @Transactional
    public Product updateProductStatus(Long id, Status status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        product.setStatus(status);
        return productRepository.save(product);
    }
    
    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    /**
     * 获取指定状态的商品
     */
    public Page<Product> findByStatus(Status status, Pageable pageable) {
        return productRepository.findByStatus(status, pageable);
    }
    
    /**
     * 获取指定分类的商品
     */
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    /**
     * 获取指定状态和分类的商品
     */
    public Page<Product> findByStatusAndCategoryId(Status status, Long categoryId, Pageable pageable) {
        return productRepository.findByStatusAndCategoryId(status, categoryId, pageable);
    }
    
    /**
     * 获取指定标签的商品
     */
    public Page<Product> findByTagId(Long tagId, Pageable pageable) {
        return productRepository.findByTagId(tagId, pageable);
    }
    
    /**
     * 搜索商品
     */
    public Page<Product> search(String keyword, Pageable pageable) {
        return productRepository.search(keyword, pageable);
    }
    
    /**
     * 获取用户创建的商品
     */
    public List<Product> findByCreatorId(Long creatorId) {
        return productRepository.findByCreatorId(creatorId);
    }
    
    /**
     * 获取当前用户创建的商品
     */
    public List<Product> findByCurrentUser() {
        User currentUser = assetService.getCurrentUser();
        return productRepository.findByCreatorId(currentUser.getId());
    }
    
    /**
     * 关联区块链资产ID到商品
     */
    @Transactional
    public Product linkAssetToProduct(Long productId, String assetId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查资产ID是否存在
        blockchainService.queryAsset(assetId)
                .thenApply(asset -> {
                    product.setAssetId(assetId);
                    return productRepository.save(product);
                })
                .exceptionally(ex -> {
                    throw new RuntimeException("区块链资产不存在", ex);
                });
        
        return product;
    }
} 