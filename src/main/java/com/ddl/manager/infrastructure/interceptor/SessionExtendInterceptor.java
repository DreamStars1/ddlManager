package com.ddl.manager.infrastructure.interceptor;

import com.ddl.manager.infrastructure.service.RedisSessionService;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Session有效期拦截器
 * 每次请求延长Session，保证在线用户统计准确
 */
@Component
@RequiredArgsConstructor
public class SessionExtendInterceptor implements HandlerInterceptor {
    private final RedisSessionService redisSessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取当前登录用户名
        String username = SecurityUtils.getCurrentUsername();
        if (username != null && !username.isEmpty()) {
            // 延长Session有效期（复用原有方法）
            redisSessionService.extendUserSession(username);
        }
        return true;
    }
}