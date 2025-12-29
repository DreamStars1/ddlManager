package com.ddl.manager.domain.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体
 * @author dzhenghaipei
 * @since 2025-12-13
 */
@Entity
@Table(name = "sys_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {

    /** 角色ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 角色标识，如 ROLE_USER, ROLE_ADMIN */
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    /** 角色名称，如 普通用户, 管理员 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 关联的用户 */
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<UserEntity> users = new HashSet<>();
}
