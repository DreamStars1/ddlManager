package com.ddl.manager.domain.auth.repository;

import com.ddl.manager.domain.auth.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户实体
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 根据UUID查找用户
     * @param uuid UUID
     * @return 用户实体
     */
    Optional<UserEntity> findByUuid(String uuid);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return true-存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return true-存在
     */
    boolean existsByEmail(String email);
}
