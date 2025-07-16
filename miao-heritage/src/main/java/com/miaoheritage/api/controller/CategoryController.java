package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.CategoryDTO;
import com.miaoheritage.entity.Category;
import com.miaoheritage.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 获取所有顶级分类
     */
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<Category> categories = categoryService.findRootCategories();
        return ResponseEntity.ok(CategoryDTO.fromEntities(categories));
    }
    
    /**
     * 获取指定父分类的子分类
     */
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<CategoryDTO>> getChildCategories(@PathVariable Long parentId) {
        List<Category> categories = categoryService.findSubCategories(parentId);
        return ResponseEntity.ok(CategoryDTO.fromEntities(categories));
    }
    
    /**
     * 获取所有活跃的分类
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllActiveCategories() {
        List<Category> categories = categoryService.findAllActive();
        return ResponseEntity.ok(CategoryDTO.fromEntities(categories));
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(CategoryDTO.fromEntity(category)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setIcon(categoryDTO.getIcon());
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
        category.setActive(true);
        
        // 处理父分类
        if (categoryDTO.getParentId() != null) {
            categoryService.findById(categoryDTO.getParentId())
                    .ifPresent(parent -> category.setParent(parent));
        }
        
        Category savedCategory = categoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryDTO.fromEntity(savedCategory));
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryDTO categoryDTO) {
        
        return categoryService.findById(id)
                .map(category -> {
                    category.setName(categoryDTO.getName());
                    category.setDescription(categoryDTO.getDescription());
                    category.setIcon(categoryDTO.getIcon());
                    
                    if (categoryDTO.getSortOrder() != null) {
                        category.setSortOrder(categoryDTO.getSortOrder());
                    }
                    
                    category.setActive(categoryDTO.isActive());
                    
                    // 更新父分类
                    if (categoryDTO.getParentId() != null) {
                        if (!categoryDTO.getParentId().equals(category.getId())) { // 防止自引用
                            categoryService.findById(categoryDTO.getParentId())
                                    .ifPresent(parent -> category.setParent(parent));
                        }
                    } else {
                        category.setParent(null);
                    }
                    
                    Category updatedCategory = categoryService.save(category);
                    return ResponseEntity.ok(CategoryDTO.fromEntity(updatedCategory));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 