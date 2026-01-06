package com.ddl.manager.shared.util;

import com.ddl.manager.domain.auth.model.SecurityUser;
import com.ddl.manager.infrastructure.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 安全工具类 - 修复 DevTools 类加载器问题
 * 实现 ApplicationContextAware 以获取 Spring 上下文
 */
@Slf4j
@Component
public class SecurityUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SecurityUtils.applicationContext = applicationContext;
    }

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
     * 获取当前用户 - 修复版本
     * 避免直接类型转换，解决 DevTools 类加载器问题
     */
    public static SecurityUser getCurrentUser() {
        try {
            Authentication authentication = getAuthentication();
            Object principal = authentication.getPrincipal();

            // 方法1：检查是否是匿名用户
            if ("anonymousUser".equals(principal)) {
                throw new BusinessException("用户未登录");
            }

            // 方法2：通过用户名从数据库重新加载用户信息
            // 这是最安全的方法，避免类加载器问题
            String username = getUsernameFromPrincipal(principal);
            return reloadUserFromDatabase(username);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            throw new BusinessException("获取用户信息失败");
        }
    }

    /**
     * 从 Principal 对象中安全提取用户名
     */
    private static String getUsernameFromPrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        } else {
            // 尝试通过反射获取用户名
            try {
                java.lang.reflect.Method getUsernameMethod = principal.getClass().getMethod("getUsername");
                Object result = getUsernameMethod.invoke(principal);
                if (result instanceof String) {
                    return (String) result;
                }
            } catch (Exception e) {
                log.warn("无法从Principal获取用户名: {}", principal.getClass().getName());
            }

            // 最后尝试调用toString()
            return principal.toString();
        }
    }

    /**
     * 从数据库重新加载用户信息
     * 避免类加载器导致的类型转换问题
     */
    private static SecurityUser reloadUserFromDatabase(String username) {
        try {
            // 通过ApplicationContext获取UserService
            com.ddl.manager.domain.auth.service.UserService userService =
                    applicationContext.getBean(com.ddl.manager.domain.auth.service.UserService.class);

            com.ddl.manager.domain.auth.model.UserEntity user = userService.findByUsername(username);
            return new SecurityUser(user);

        } catch (Exception e) {
            log.error("从数据库重新加载用户失败: {}", username, e);
            throw new BusinessException("重新加载用户信息失败: " + e.getMessage());
        }
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

    /**
     * 直接从SecurityContext获取用户ID（不重新加载数据库）
     * 用于性能要求高的场景
     */
    public static Long getCurrentUserIdFast() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();
            if ("anonymousUser".equals(principal)) {
                return null;
            }

            // 尝试通过反射快速获取用户ID
            if (principal instanceof UserDetails) {
                try {
                    java.lang.reflect.Method getUserIdMethod = principal.getClass().getMethod("getUserId");
                    Object result = getUserIdMethod.invoke(principal);
                    if (result instanceof Long) {
                        return (Long) result;
                    }
                } catch (Exception e) {
                    // 忽略反射错误，回退到完整加载
                }
            }

            // 回退到完整加载
            return getCurrentUserId();

        } catch (Exception e) {
            log.warn("快速获取用户ID失败，回退到完整加载", e);
            return getCurrentUserId();
        }
    }
}