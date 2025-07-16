package com.miaoheritage.service;

import com.miaoheritage.entity.Category;
import com.miaoheritage.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * 获取所有顶级分类
     */
    public List<Category> findRootCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrder();
    }
    
    /**
     * 获取指定父分类的子分类
     */
    public List<Category> findSubCategories(Long parentId) {
        return categoryRepository.findByParentIdOrderBySortOrder(parentId);
    }
    
    /**
     * 获取分类详情
     */
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * 通过名称查找分类
     */
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    /**
     * 创建或更新分类
     */
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
    
    /**
     * 获取所有活跃的分类
     */
    public List<Category> findAllActive() {
        return categoryRepository.findByIsActiveTrue();
    }
    
    /**
     * 获取所有分类
     */
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
} 