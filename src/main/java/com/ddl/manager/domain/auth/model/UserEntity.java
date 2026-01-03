package com.ddl.manager.domain.auth.model;

import com.ddl.manager.shared.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户实体
 * 必须实现Serializable接口以支持Redis序列化
 * @author developer
 * @since 2025-12-13
 */
@Entity
@Table(name = "sys_user", indexes = {
        @Index(name = "idx_user_uuid", columnList = "uuid"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** 密码（加密存储） */
    @Column(nullable = false)
    private String password;

    /** 邮箱 */
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /** 账户状态：true-启用，false-禁用 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /** 提醒阈值（小时），默认24小时前提醒 */
    @Column(nullable = false)
    @Builder.Default
    private Integer reminderHours = 24;

    /** 是否开启邮件提醒 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailNotificationEnabled = true;

    /** 用户角色 */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    /**
     * 转换为SecurityUser
     */
    public SecurityUser toSecurityUser() {
        return new SecurityUser(this);
    }
}