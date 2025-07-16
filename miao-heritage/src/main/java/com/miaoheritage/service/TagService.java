package com.miaoheritage.service;

import com.miaoheritage.entity.Tag;
import com.miaoheritage.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {
    
    private final TagRepository tagRepository;
    
    /**
     * 获取所有标签
     */
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }
    
    /**
     * 根据ID获取标签
     */
    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }
    
    /**
     * 根据名称获取标签
     */
    public Optional<Tag> findByName(String name) {
        return tagRepository.findByName(name);
    }
    
    /**
     * 搜索标签
     */
    public List<Tag> searchByKeyword(String keyword) {
        return tagRepository.searchByKeyword(keyword);
    }
    
    /**
     * 获取产品关联的标签
     */
    public List<Tag> findByProductId(Long productId) {
        return tagRepository.findByProductId(productId);
    }
    
    /**
     * 创建或更新标签
     */
    @Transactional
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }
    
    /**
     * 创建标签（如果不存在）
     */
    @Transactional
    public Tag createIfNotExists(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(name);
                    return tagRepository.save(newTag);
                });
    }
    
    /**
     * 删除标签
     */
    @Transactional
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }
} 