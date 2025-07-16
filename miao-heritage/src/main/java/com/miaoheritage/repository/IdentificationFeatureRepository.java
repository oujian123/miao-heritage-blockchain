package com.miaoheritage.repository;

import com.miaoheritage.entity.IdentificationFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentificationFeatureRepository extends JpaRepository<IdentificationFeature, Long> {
    
    List<IdentificationFeature> findByIdentificationRecordId(Long recordId);
    
    List<IdentificationFeature> findByIdentificationRecordIdAndIsAuthenticIndicator(Long recordId, Boolean isAuthenticIndicator);
} 