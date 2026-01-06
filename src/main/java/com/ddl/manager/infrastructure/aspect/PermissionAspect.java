package com.ddl.manager.infrastructure.aspect;

import com.ddl.manager.domain.auth.model.SecurityUser;
import com.ddl.manager.infrastructure.annotation.RequiresPermission;
import com.ddl.manager.infrastructure.exception.BusinessException;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限控制切面
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    /**
     * 环绕通知：拦截标记@RequiresPermission的方法
     */
    @Around("@annotation(requiresPermission)")
    public Object around(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        // 1. 获取当前用户
        SecurityUser currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }

        // 2. 获取用户权限
        Collection<? extends GrantedAuthority> authorities = currentUser.getAuthorities();
        List<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 3. 检查权限
        boolean hasPermission = checkPermission(requiresPermission, userRoles);

        if (!hasPermission) {
            String methodName = getMethodName(joinPoint);
            log.warn("用户 {} 无权限访问方法: {}", currentUser.getUsername(), methodName);
            throw new BusinessException("权限不足，无法执行此操作");
        }

        // 4. 执行原方法
        return joinPoint.proceed();
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(RequiresPermission annotation, List<String> userRoles) {
        String[] requiredRoles = annotation.roles();

        if (requiredRoles.length == 0) {
            return true; // 没有设置角色要求，默认通过
        }

        boolean hasRequiredRole = Arrays.stream(requiredRoles)
                .anyMatch(userRoles::contains);

        if (annotation.logical() == RequiresPermission.Logical.OR) {
            return hasRequiredRole; // OR逻辑：有一个角色满足即可
        } else {
            // AND逻辑：需要满足所有角色
            return Arrays.stream(requiredRoles).allMatch(userRoles::contains);
        }
    }

    /**
     * 获取方法名
     */
    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
}