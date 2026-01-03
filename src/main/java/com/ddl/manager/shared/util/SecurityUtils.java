package com.ddl.manager.shared.util;

import com.ddl.manager.domain.auth.model.SecurityUser;
import com.ddl.manager.infrastructure.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 * 正确获取当前登录用户信息
 */
@Slf4j
@Component
public class SecurityUtils {

    /**
     * 获取当前认证信息
     */
    public static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        return authentication;
    }

    /**
     * 获取当前用户
     */
    public static SecurityUser getCurrentUser() {
        Authentication authentication = getAuthentication();
        Object principal = authentication.getPrincipal();

        // 方法1：检查是否是 UserDetails 接口的实现
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            // 通过类名检查确保是 SecurityUser 类型
            if (userDetails.getClass().getName().equals(SecurityUser.class.getName())) {
                return (SecurityUser) userDetails;
            }
        }

        // 方法2：直接通过类名比较（更安全的方式）
        if (principal != null && SecurityUser.class.getName().equals(principal.getClass().getName())) {
            return (SecurityUser) principal;
        }

        // 方法3：检查匿名用户
        if ("anonymousUser".equals(principal)) {
            throw new BusinessException("用户未登录");
        }

        log.error("未知的Principal类型: {}, 类加载器: {}",
                principal.getClass().getName(),
                principal.getClass().getClassLoader());
        throw new BusinessException("用户身份信息异常");
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        SecurityUser user = getCurrentUser();
        return user.getUserId();
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        SecurityUser user = getCurrentUser();
        return user.getUsername();
    }

    /**
     * 检查用户是否登录
     */
    public static boolean isLoggedIn() {
        try {
            getCurrentUser();
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }
}