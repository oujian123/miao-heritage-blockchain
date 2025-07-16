package com.miaoheritage.repository;

import com.miaoheritage.entity.IdentificationRecord;
import com.miaoheritage.entity.IdentificationRecord.ProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentificationRecordRepository extends JpaRepository<IdentificationRecord, Long> {
    
    List<IdentificationRecord> findByUserId(Long userId);
    
    Page<IdentificationRecord> findByUserId(Long userId, Pageable pageable);
    
    List<IdentificationRecord> findByUserIdAndStatus(Long userId, ProcessingStatus status);
    
    Page<IdentificationRecord> findByStatus(ProcessingStatus status, Pageable pageable);
} 