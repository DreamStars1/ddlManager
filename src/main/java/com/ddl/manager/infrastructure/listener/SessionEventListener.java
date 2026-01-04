package com.ddl.manager.infrastructure.listener;

import com.ddl.manager.infrastructure.service.RedisSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.stereotype.Component;

/**
 * Session事件监听器
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@Component
public class SessionEventListener {

    @Autowired
    private RedisSessionService redisSessionService;

    /**
     * 监听Session创建事件
     */
    @EventListener
    public void onSessionCreated(SessionCreatedEvent event) {
        String sessionId = event.getSessionId();
        log.info("Session创建: {}", sessionId);

        // 可以在这里记录登录日志或执行其他业务逻辑
    }

    /**
     * 监听Session删除事件（用户主动退出）
     */
    @EventListener
    public void onSessionDeleted(SessionDeletedEvent event) {
        String sessionId = event.getSessionId();
        log.info("Session删除: {}", sessionId);

        // 清理相关的业务数据
        cleanupSessionData(sessionId);
    }

    /**
     * 监听Session过期事件
     */
    @EventListener
    public void onSessionExpired(SessionExpiredEvent event) {
        String sessionId = event.getSessionId();
        log.info("Session过期: {}", sessionId);

        // 清理相关的业务数据
        cleanupSessionData(sessionId);
    }

    /**
     * 清理Session相关数据
     */
    private void cleanupSessionData(String sessionId) {
        // 这里可以根据sessionId清理相关的业务数据
        // 比如：用户活动记录、临时数据等
    }
}