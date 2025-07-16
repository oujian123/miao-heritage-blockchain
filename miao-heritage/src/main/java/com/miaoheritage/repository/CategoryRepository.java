package com.miaoheritage.repository;

import com.miaoheritage.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByParentIsNullOrderBySortOrder();
    
    List<Category> findByParentIdOrderBySortOrder(Long parentId);
    
    Optional<Category> findByName(String name);
    
    List<Category> findByIsActiveTrue();
} 