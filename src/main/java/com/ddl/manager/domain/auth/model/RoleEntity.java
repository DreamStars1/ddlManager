package com.ddl.manager.domain.auth.model;

import com.ddl.manager.shared.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体
 * 必须实现Serializable接口以支持Redis序列化
 * @author developer
 * @since 2025-12-13
 */
@Entity
@Table(name = "sys_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色代码，如 ROLE_ADMIN, ROLE_USER */
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    /** 角色名称，如 管理员, 普通用户 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 角色描述 */
    @Column(length = 200)
    private String description;
}