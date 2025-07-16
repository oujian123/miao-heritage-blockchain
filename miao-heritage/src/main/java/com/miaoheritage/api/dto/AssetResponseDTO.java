package com.miaoheritage.api.dto;

import com.miaoheritage.blockchain.model.AssetHistoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponseDTO {
    private String id;
    private String type;
    private String name;
    private String description;
    private String owner;
    private String artisan;
    private String materialSource;
    private LocalDateTime createTime;
    private List<AssetHistoryDTO> history;
    private Map<String, Object> attributes;
    private String qrCodeUrl;
    private String imageUrl;
    
    // 时间戳转换为LocalDateTime
    public void setCreateTimestamp(long timestamp) {
        this.createTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), 
            ZoneId.systemDefault()
        );
    }
} 