package com.ddl.manager.domain.auth.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Spring Security用户详情
 * @author zhenghaipei
 * @since 2025-12-13
 */
public class SecurityUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** 用户实体 */
    private final UserEntity user;

    public SecurityUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public String getUseravatar(){ return user.getAvatar();}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 获取用户UUID
     * @return 用户UUID
     */
    public String getUserUuid() {
        return user.getUuid();
    }

    /**
     * 获取用户邮箱
     * @return 邮箱
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 获取用户实体
     * @return 用户实体
     */
    public UserEntity getUser() {
        return user;
    }
}
