package com.ddl.manager.domain.auth.controller;

import com.ddl.manager.shared.dto.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Session管理控制器
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 清理所有Session数据
     * 用于修复登录问题
     */
    @PostMapping("/clean")
    public AjaxResult cleanAllSessions() {
        try {
            // 清理Spring Session相关数据
            Set<String> keys = redisTemplate.keys("spring:session:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清理Session数据完成，共清理 {} 个key", keys.size());
            }

            // 清理应用自定义的Session数据
            Set<String> appKeys = redisTemplate.keys("session:*");
            if (appKeys != null && !appKeys.isEmpty()) {
                redisTemplate.delete(appKeys);
            }

            return AjaxResult.ok("Session数据清理完成", "清理key数量: " + keys.size());
        } catch (Exception e) {
            log.error("清理Session数据失败", e);
            return AjaxResult.error("清理Session数据失败: " + e.getMessage());
        }
    }

    /**
     * 检查Redis连接状态
     */
    @PostMapping("/check")
    public AjaxResult checkRedisConnection() {
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().set("test:connection", "OK", 10);
            String result = (String) redisTemplate.opsForValue().get("test:connection");

            if ("OK".equals(result)) {
                return AjaxResult.ok("Redis连接正常");
            } else {
                return AjaxResult.error("Redis连接异常");
            }
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            return AjaxResult.error("Redis连接失败: " + e.getMessage());
        }
    }
}