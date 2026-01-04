package com.ddl.manager.domain.auth.service;

import com.ddl.manager.domain.auth.dto.RegisterDTO;
import com.ddl.manager.domain.auth.model.RoleEntity;
import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.repository.RoleRepository;
import com.ddl.manager.domain.auth.repository.UserRepository;
import com.ddl.manager.infrastructure.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户服务实现类
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册后的用户实体
     */
    @Override
    @Transactional
    public UserEntity register(RegisterDTO registerDTO) {
        // 1. 校验密码确认
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 2. 检查用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 4. 获取默认角色（ROLE_USER）
        RoleEntity userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new BusinessException("系统角色配置错误，请联系管理员"));

        // 5. 创建用户实体
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);

        UserEntity user = UserEntity.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .email(registerDTO.getEmail())
                .enabled(true)
                .reminderHours(24)  // 默认24小时前提醒
                .emailNotificationEnabled(true)  // 默认开启邮件提醒
                .roles(roles)
                .build();

        // 6. 保存用户
        UserEntity savedUser = userRepository.save(user);
        log.info("用户注册成功: username={}, email={}", savedUser.getUsername(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    @Override
    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在: " + username));
    }

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户实体
     */
    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("用户不存在: " + email));
    }
}





