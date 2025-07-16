package com.miaoheritage.config;

import com.miaoheritage.entity.Role;
import com.miaoheritage.entity.Role.ERole;
import com.miaoheritage.entity.User;
import com.miaoheritage.repository.RoleRepository;
import com.miaoheritage.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("初始化基础数据...");
            
            // 初始化角色
            initRoles();
            
            // 创建管理员账号
            createAdminIfNotExist();
            
            log.info("数据初始化完成!");
        };
    }
    
    private void initRoles() {
        // 检查并创建系统角色
        for (ERole roleName : ERole.values()) {
            if (!roleRepository.findByName(roleName).isPresent()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("创建角色: {}", roleName);
            }
        }
    }
    
    private void createAdminIfNotExist() {
        String adminUsername = env.getProperty("admin.username", "admin");
        String adminEmail = env.getProperty("admin.email", "admin@miao-heritage.com");
        
        if (!userService.existsByUsername(adminUsername)) {
            // 创建管理员用户
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword("admin123"); // 会在service中加密
            admin.setFullName("系统管理员");
            admin.setVerified(true);
            
            // 设置角色
            Set<ERole> adminRoles = new HashSet<>();
            adminRoles.add(ERole.ROLE_ADMIN);
            
            // 保存用户
            userService.createUser(admin, adminRoles);
            
            log.info("创建管理员账号: {}", adminUsername);
        }
    }
} 