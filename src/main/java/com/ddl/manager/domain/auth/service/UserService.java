package com.ddl.manager.domain.auth.service;

import com.ddl.manager.domain.auth.dto.RegisterDTO;
import com.ddl.manager.domain.auth.model.UserEntity;

/**
 * 用户服务接口
 * @author zhenghaipei
 * @since 2025-12-14
 */
public interface UserService {

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 注册后的用户实体
     */
    UserEntity register(RegisterDTO registerDTO);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    UserEntity findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户实体
     */
    UserEntity findByEmail(String email);
}

