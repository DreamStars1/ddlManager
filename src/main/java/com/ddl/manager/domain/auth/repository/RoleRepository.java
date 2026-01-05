package com.ddl.manager.domain.auth.repository;

import com.ddl.manager.domain.auth.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    /**
     * 根据角色标识查找角色
     * @param code 角色标识
     * @return 角色实体
     */
    Optional<RoleEntity> findByCode(String code);
}
