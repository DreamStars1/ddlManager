package com.ddl.manager.infrastructure.config;

import com.ddl.manager.domain.auth.model.RoleEntity;
import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.repository.RoleRepository;
import com.ddl.manager.domain.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据初始化器
 * 系统启动时自动创建测试用户和角色
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initRoles();
        initTestUsers();
    }

    /**
     * 初始化角色
     */
    private void initRoles() {
        if (roleRepository.count() == 0) {
            RoleEntity userRole = RoleEntity.builder()
                    .code("ROLE_USER")
                    .name("普通用户")
                    .build();
            roleRepository.save(userRole);

            RoleEntity adminRole = RoleEntity.builder()
                    .code("ROLE_ADMIN")
                    .name("管理员")
                    .build();
            roleRepository.save(adminRole);

            log.info("初始化角色完成: ROLE_USER, ROLE_ADMIN");
        }
    }

    /**
     * 初始化测试用户
     */
    private void initTestUsers() {
        // 创建普通用户
        if (!userRepository.existsByUsername("user")) {
            RoleEntity userRole = roleRepository.findByCode("ROLE_USER").orElse(null);
            Set<RoleEntity> roles = new HashSet<>();
            if (userRole != null) {
                roles.add(userRole);
            }

            UserEntity user = UserEntity.builder()
                    .username("user")
                    .password(passwordEncoder.encode("123456"))
                    .email("user@test.com")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(user);
            log.info("创建测试用户: user / 123456");
        }

        // 创建管理员用户
        if (!userRepository.existsByUsername("admin")) {
            RoleEntity adminRole = roleRepository.findByCode("ROLE_ADMIN").orElse(null);
            RoleEntity userRole = roleRepository.findByCode("ROLE_USER").orElse(null);
            Set<RoleEntity> roles = new HashSet<>();
            if (adminRole != null) {
                roles.add(adminRole);
            }
            if (userRole != null) {
                roles.add(userRole);
            }

            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@test.com")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(admin);
            log.info("创建管理员用户: admin / admin123");
        }
    }
}
