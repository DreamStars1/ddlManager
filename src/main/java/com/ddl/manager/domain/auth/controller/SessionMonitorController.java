//package com.ddl.manager.domain.auth.controller;
//
//import com.ddl.manager.infrastructure.service.RedisSessionService;
//import com.ddl.manager.shared.dto.AjaxResult;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Session监控控制器
// * @author developer
// * @since 2025-12-14
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/admin/session")
//@PreAuthorize("hasRole('ADMIN')")
//public class SessionMonitorController {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Autowired
//    private RedisSessionService redisSessionService;
//
//    /**
//     * 获取在线用户统计
//     */
//    @GetMapping("/statistics")
//    public AjaxResult getSessionStatistics() {
//        try {
//            long onlineCount = redisSessionService.getOnlineUserCount();
//            long totalSessions = getTotalSessionCount();
//
//            Map<String, Object> data = new HashMap<>();
//            data.put("onlineUsers", onlineCount);
//            data.put("totalSessions", totalSessions);
//            data.put("timestamp", System.currentTimeMillis());
//
//            return AjaxResult.ok("获取会话统计成功", data);
//        } catch (Exception e) {
//            log.error("获取会话统计失败", e);
//            return AjaxResult.error("获取会话统计失败");
//        }
//    }
//
//    /**
//     * 清理所有Session数据
//     */
//    @GetMapping("/clean-all")
//    public AjaxResult cleanAllSessions() {
//        try {
//            // 清理Spring Session相关数据
//            Set<String> keys = redisTemplate.keys("spring:session:*");
//            int springSessionCount = keys != null ? keys.size() : 0;
//            if (keys != null && !keys.isEmpty()) {
//                redisTemplate.delete(keys);
//            }
//
//            // 清理应用自定义的Session数据
//            Set<String> userSessionKeys = redisTemplate.keys("ddl:user:*");
//            int userSessionCount = userSessionKeys != null ? userSessionKeys.size() : 0;
//            if (userSessionKeys != null && !userSessionKeys.isEmpty()) {
//                redisTemplate.delete(userSessionKeys);
//            }
//
//            Map<String, Integer> data = new HashMap<>();
//            data.put("cleanedSpringSessions", springSessionCount);
//            data.put("cleanedUserSessions", userSessionCount);
//
//            return AjaxResult.ok("所有Session数据清理完成", data);
//        } catch (Exception e) {
//            log.error("清理Session数据失败", e);
//            return AjaxResult.error("清理Session数据失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 强制用户下线
//     */
//    @GetMapping("/force-logout")
//    public AjaxResult forceLogout(String username) {
//        try {
//            redisSessionService.deleteUserSession(username);
//
//            // 这里可以添加额外的逻辑，比如发送消息通知用户被强制下线
//
//            return AjaxResult.ok("用户已强制下线: " + username);
//        } catch (Exception e) {
//            log.error("强制用户下线失败", e);
//            return AjaxResult.error("强制用户下线失败: " + e.getMessage());
//        }
//    }
//
//    private long getTotalSessionCount() {
//        try {
//            Set<String> keys = redisTemplate.keys("spring:session:sessions:*");
//            return keys != null ? keys.size() : 0;
//        } catch (Exception e) {
//            log.error("获取总Session数量失败", e);
//            return 0;
//        }
//    }
//}