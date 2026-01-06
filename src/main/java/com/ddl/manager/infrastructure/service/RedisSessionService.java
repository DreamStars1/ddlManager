package com.ddl.manager.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis Session管理服务
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@Service
public class RedisSessionService {

    private static final String USER_SESSION_PREFIX = "ddl:user:sessions:";
    private static final String USER_PERMISSION_PREFIX = "ddl:user:permissions:";
    private static final long SESSION_TTL = 1800; // 30分钟

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 存储用户Session信息到Redis
     */
    public void storeUserSession(String username, Object sessionData) {
        String key = USER_SESSION_PREFIX + username;
        try {
            redisTemplate.opsForValue().set(key, sessionData, SESSION_TTL, TimeUnit.SECONDS);
            log.debug("存储用户Session到Redis: {}", username);
        } catch (Exception e) {
            log.error("存储用户Session失败: {}", username, e);
        }
    }

    /**
     * 从Redis获取用户Session信息
     */
    public Object getUserSession(String username) {
        String key = USER_SESSION_PREFIX + username;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取用户Session失败: {}", username, e);
            return null;
        }
    }

    /**
     * 存储用户权限信息到Redis
     */
    public void storeUserPermissions(String username, Object permissions) {
        String key = USER_PERMISSION_PREFIX + username;
        try {
            redisTemplate.opsForValue().set(key, permissions, SESSION_TTL, TimeUnit.SECONDS);
            log.debug("存储用户权限到Redis: {}", username);
        } catch (Exception e) {
            log.error("存储用户权限失败: {}", username, e);
        }
    }

    /**
     * 从Redis获取用户权限信息
     */
    public Object getUserPermissions(String username) {
        String key = USER_PERMISSION_PREFIX + username;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", username, e);
            return null;
        }
    }

    /**
     * 删除用户Session和权限信息
     */
    public void deleteUserSession(String username) {
        String sessionKey = USER_SESSION_PREFIX + username;
        String permissionKey = USER_PERMISSION_PREFIX + username;

        try {
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(permissionKey);
            log.debug("删除用户Session和权限: {}", username);
        } catch (Exception e) {
            log.error("删除用户Session失败: {}", username, e);
        }
    }

    /**
     * 延长用户Session有效期
     */
    public void extendUserSession(String username) {
        String sessionKey = USER_SESSION_PREFIX + username;
        String permissionKey = USER_PERMISSION_PREFIX + username;

        try {
            redisTemplate.expire(sessionKey, SESSION_TTL, TimeUnit.SECONDS);
            redisTemplate.expire(permissionKey, SESSION_TTL, TimeUnit.SECONDS);
            log.debug("延长用户Session有效期: {}", username);
        } catch (Exception e) {
            log.error("延长用户Session有效期失败: {}", username, e);
        }
    }

    /**
     * 获取在线用户数量
     */
    public long getOnlineUserCount() {
        try {
            String pattern = USER_SESSION_PREFIX + "*";
            return redisTemplate.keys(pattern).size();
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return 0;
        }
    }
}