package com.miaoheritage.api.controller;

import com.miaoheritage.api.dto.JwtResponse;
import com.miaoheritage.api.dto.LoginRequest;
import com.miaoheritage.api.dto.MessageResponse;
import com.miaoheritage.api.dto.RegisterRequest;
import com.miaoheritage.entity.Role;
import com.miaoheritage.entity.Role.ERole;
import com.miaoheritage.entity.User;
import com.miaoheritage.repository.RoleRepository;
import com.miaoheritage.security.JwtUtils;
import com.miaoheritage.security.UserDetailsImpl;
import com.miaoheritage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        // 设置认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成JWT令牌
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // 获取用户详情
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 获取角色列表
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        
        // 返回JWT响应
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                roles));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("错误: 用户名已被使用!"));
        }
        
        // 检查邮箱是否已存在
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("错误: 邮箱已被使用!"));
        }
        
        // 创建用户对象
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setFullName(registerRequest.getFullName());
        user.setPhone(registerRequest.getPhone());
        user.setVerified(false); // 默认未验证
        
        // 处理角色
        Set<Role.ERole> roleNames = new HashSet<>();
        
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            roleNames.add(ERole.ROLE_USER); // 默认角色
        } else {
            registerRequest.getRoles().forEach(role -> {
                switch (role) {
                    case "admin":
                        roleNames.add(ERole.ROLE_ADMIN);
                        break;
                    case "artisan":
                        roleNames.add(ERole.ROLE_ARTISAN);
                        break;
                    case "merchant":
                        roleNames.add(ERole.ROLE_MERCHANT);
                        break;
                    case "certifier":
                        roleNames.add(ERole.ROLE_CERTIFIER);
                        break;
                    default:
                        roleNames.add(ERole.ROLE_USER);
                }
            });
        }
        
        // 创建并保存用户
        userService.createUser(user, roleNames);
        
        return ResponseEntity.ok(new MessageResponse("用户注册成功!"));
    }
} 