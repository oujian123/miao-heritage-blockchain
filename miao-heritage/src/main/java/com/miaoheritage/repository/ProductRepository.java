package com.miaoheritage.repository;

import com.miaoheritage.entity.Product;
import com.miaoheritage.entity.Product.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByStatus(Status status, Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.category.id = :categoryId")
    Page<Product> findByStatusAndCategoryId(@Param("status") Status status, @Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN p.tags t WHERE t.id = :tagId")
    Page<Product> findByTagId(@Param("tagId") Long tagId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);
    
    Optional<Product> findByAssetId(String assetId);
    
    List<Product> findByCreatorId(Long creatorId);
} 