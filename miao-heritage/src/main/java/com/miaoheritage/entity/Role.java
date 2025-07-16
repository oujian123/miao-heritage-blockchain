package com.miaoheritage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private ERole name;
    
    public enum ERole {
        ROLE_USER,       // 普通用户
        ROLE_ARTISAN,    // 匠人/创作者
        ROLE_MERCHANT,   // 商家
        ROLE_CERTIFIER,  // 认证机构
        ROLE_ADMIN       // 管理员
    }
} 