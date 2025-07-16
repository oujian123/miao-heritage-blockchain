package com.miaoheritage.service;

import com.miaoheritage.entity.Role;
import com.miaoheritage.entity.User;
import com.miaoheritage.repository.RoleRepository;
import com.miaoheritage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 创建新用户
     */
    @Transactional
    public User createUser(User user, Set<Role.ERole> roleNames) {
        // 对密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 获取角色
        Set<Role> roles = new HashSet<>();
        
        if (roleNames == null || roleNames.isEmpty()) {
            // 默认角色：普通用户
            roleRepository.findByName(Role.ERole.ROLE_USER)
                    .ifPresent(roles::add);
        } else {
            roleNames.forEach(roleName -> 
                roleRepository.findByName(roleName)
                    .ifPresent(roles::add));
        }
        
        user.setRoles(roles);
        
        // 保存用户
        return userRepository.save(user);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * 通过ID查找用户
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 通过用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 通过邮箱查找用户
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * 检查用户名是否已存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否已存在
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 通过区块链身份查找用户
     */
    public Optional<User> findByBlockchainIdentity(String blockchainIdentity) {
        return userRepository.findByBlockchainIdentity(blockchainIdentity);
    }
    
    /**
     * 获取所有用户列表
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
} 