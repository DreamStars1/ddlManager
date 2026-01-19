package com.ddl.manager.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
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
    private static final String USER_LOGIN_TODAY_PREFIX = "ddl:user:login:today:"; // 新增：今日登录用户前缀
    private static final String TOTAL_USER_COUNT_KEY = "ddl:user:total:count";     // 新增：总用户数key
    private static final long SESSION_TTL = 1800; // 30分钟
    private static final long LOGIN_TODAY_TTL = 86400; // 今日登录记录过期时间（24小时）

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void storeUserSession(String username, Object sessionData) {
        String key = USER_SESSION_PREFIX + username;
        try {
            redisTemplate.opsForValue().set(key, sessionData, SESSION_TTL, TimeUnit.SECONDS);
            log.debug("存储用户Session到Redis: {}", username);
        } catch (Exception e) {
            log.error("存储用户Session失败: {}", username, e);
        }
    }

    public Object getUserSession(String username) {
        String key = USER_SESSION_PREFIX + username;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取用户Session失败: {}", username, e);
            return null;
        }
    }

    public void storeUserPermissions(String username, Object permissions) {
        String key = USER_PERMISSION_PREFIX + username;
        try {
            redisTemplate.opsForValue().set(key, permissions, SESSION_TTL, TimeUnit.SECONDS);
            log.debug("存储用户权限到Redis: {}", username);
        } catch (Exception e) {
            log.error("存储用户权限失败: {}", username, e);
        }
    }

    public Object getUserPermissions(String username) {
        String key = USER_PERMISSION_PREFIX + username;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", username, e);
            return null;
        }
    }

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

    public long getOnlineUserCount() {
        try {
            String pattern = USER_SESSION_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            return keys == null ? 0 : keys.size();
        } catch (Exception e) {
            log.error("获取在线用户数量失败", e);
            return 0;
        }
    }

    /**
     * 记录用户今日登录（登录成功时调用）
     */
    public void recordUserLoginToday(String username) {
        String todayKey = USER_LOGIN_TODAY_PREFIX + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        try {
            // 用Set存储今日登录用户，自动去重
            redisTemplate.opsForSet().add(todayKey, username);
            redisTemplate.expire(todayKey, LOGIN_TODAY_TTL, TimeUnit.SECONDS);
            log.debug("记录用户今日登录: {}", username);
        } catch (Exception e) {
            log.error("记录用户今日登录失败: {}", username, e);
        }
    }

    /**
     * 获取今日登录用户数
     */
    public long getTodayLoginCount() {
        String todayKey = USER_LOGIN_TODAY_PREFIX + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        try {
            Long size = redisTemplate.opsForSet().size(todayKey);
            return size == null ? 0 : size;
        } catch (Exception e) {
            log.error("获取今日登录用户数失败", e);
            return 0;
        }
    }

    /**
     * 更新总用户数到Redis（用户注册/删除时调用）
     */
    public void updateTotalUserCount(Long totalCount) {
        try {
            redisTemplate.opsForValue().set(TOTAL_USER_COUNT_KEY, totalCount);
            log.debug("更新总用户数到Redis: {}", totalCount);
        } catch (Exception e) {
            log.error("更新总用户数失败", e);
        }
    }

    /**
     * 获取总用户数（优先Redis，无则返回0）
     */
    public Long getTotalUserCount() {
        try {
            Object countObj = redisTemplate.opsForValue().get(TOTAL_USER_COUNT_KEY);
            return countObj == null ? 0 : Long.parseLong(countObj.toString());
        } catch (Exception e) {
            log.error("获取总用户数失败", e);
            return 0L;
        }
    }
}