package com.ddl.manager.domain.auth.service;

import com.ddl.manager.domain.auth.model.SecurityUser;
import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.repository.UserRepository;
import com.ddl.manager.infrastructure.service.RedisSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务 - 增强Redis集成
 * @author developer
 * @since 2025-12-13
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisSessionService redisSessionService;

    /**
     * 根据用户名加载用户，并存储权限到Redis
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        SecurityUser securityUser = new SecurityUser(user);

        // 存储用户权限信息到Redis
        storeUserPermissionsToRedis(securityUser);

        return securityUser;
    }

    /**
     * 存储用户权限信息到Redis
     */
    private void storeUserPermissionsToRedis(SecurityUser securityUser) {
        try {
            Map<String, Object> permissionData = new HashMap<>();
            permissionData.put("userId", securityUser.getUserId());
            permissionData.put("username", securityUser.getUsername());
            permissionData.put("authorities", securityUser.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toList()));
            permissionData.put("enabled", securityUser.isEnabled());

            redisSessionService.storeUserPermissions(securityUser.getUsername(), permissionData);
            log.info("用户权限信息已存储到Redis: {}", securityUser.getUsername());
        } catch (Exception e) {
            log.error("存储用户权限信息到Redis失败: {}", securityUser.getUsername(), e);
        }
    }

    /**
     * 从Redis重新加载用户权限（用于权限更新时）
     */
    public void reloadUserPermissions(String username) {
        try {
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

            SecurityUser securityUser = new SecurityUser(user);
            storeUserPermissionsToRedis(securityUser);
            log.info("用户权限信息已重新加载到Redis: {}", username);
        } catch (Exception e) {
            log.error("重新加载用户权限信息失败: {}", username, e);
        }
    }
}