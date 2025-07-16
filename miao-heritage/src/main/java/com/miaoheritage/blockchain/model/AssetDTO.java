package com.miaoheritage.blockchain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetDTO {
    private String id;             // 资产唯一ID
    private String type;           // 类型：银饰/服饰
    private String name;           // 名称
    private String description;    // 描述
    private String owner;          // 当前所有者
    private String artisan;        // 制作匠人
    private String artisanId;      // 匠人身份识别
    private String materialSource; // 材料来源
    private long createTime;       // 创建时间戳
    private String certHash;       // 鉴定证书哈希
    private String imageHash;      // 图片哈希
    private List<AssetHistoryDTO> history; // 历史记录
    private Map<String, Object> attributes; // 扩展属性
} 