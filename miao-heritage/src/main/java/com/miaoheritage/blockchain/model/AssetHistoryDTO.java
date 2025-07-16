package com.miaoheritage.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetHistoryDTO {
    private long timestamp;   // 时间戳
    private String operation; // 操作
    private String from;      // 操作发起方
    private String to;        // 操作接收方
    private String details;   // 详细信息
} 