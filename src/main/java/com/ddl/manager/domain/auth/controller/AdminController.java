package com.ddl.manager.domain.auth.controller;

import com.ddl.manager.common.result.Result;
import com.ddl.manager.domain.auth.dto.SessionStatsDTO;
import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.service.UserService;
import com.ddl.manager.infrastructure.annotation.RequiresPermission;
import com.ddl.manager.infrastructure.service.RedisSessionService;
import com.ddl.manager.shared.dto.AjaxResult;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 管理员控制器 - 前后端接口适配版本
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisSessionService redisSessionService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取用户列表接口
     * @param page 页码（默认0，从0开始）
     * @param size 每页条数（默认10）
     * @return 分页用户列表结果
     * 对应前端：getUsers: () => api.get('/admin/users')
     * 示例请求：GET /admin/users?page=0&size=10
     * 示例响应数据：
     * {
     *   "code": 200,
     *   "msg": "获取用户列表成功",
     *   "data": {
     *     "content": [
     *       {
     *         "id": 1,
     *         "username": "admin",
     *         "status": "ENABLED",
     *         "createTime": "2026-01-19 10:00:00"
     *       },
     *       {
     *         "id": 2,
     *         "username": "test",
     *         "status": "DISABLED",
     *         "createTime": "2026-01-18 15:30:00"
     *       }
     *     ],
     *     "pageable": {
     *       "pageNumber": 0,
     *       "pageSize": 10
     *     },
     *     "totalElements": 2,
     *     "totalPages": 1
     *   }
     * }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public Result<Page<UserEntity>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 构建分页参数，按创建时间降序排序
            Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
            // 调用服务层获取分页用户列表（需确保UserService实现getAllUsers方法）
            Page<UserEntity> userPage = userService.getAllUsers(pageable);

            log.info("管理员 {} 获取用户列表成功，页码：{}，每页条数：{}",
                    SecurityUtils.getCurrentUsername(), page, size);
            return Result.success("获取用户列表成功", userPage);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 切换用户状态接口（启用/禁用）
     * @param userId 用户ID
     * @return 状态切换结果
     * 对应前端：toggleUserStatus: (userId) => api.patch(`/admin/users/${userId}/status`)
     * 示例请求：PATCH /admin/users/1/status
     * 示例响应数据：
     * {
     *   "code": 200,
     *   "msg": "用户禁用",
     *   "data": false
     * }
     * 异常响应示例：
     * {
     *   "code": 500,
     *   "msg": "用户状态切换失败：用户不存在",
     *   "data": null
     * }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{userId}/status")
    public Result<Boolean> toggleUserStatus(@PathVariable Long userId) {
        try {
            // 调用服务层切换用户状态（需确保UserService实现toggleUserStatus方法）
            boolean isSuccess = userService.toggleUserStatus(userId);
            log.info("管理员 {} 切换用户 {} 状态成功",
                    SecurityUtils.getCurrentUsername(), userId);
            if (isSuccess) {
                return Result.success("用户启用", true);
            } else {
                return Result.success("用户禁用", false);
            }
        } catch (Exception e) {
            log.error("切换用户 {} 状态失败", userId, e);
            return Result.error("用户状态切换失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话统计数据接口
     * @return 会话统计结果
     * 对应前端：getSessionStats: () => api.get('/admin/session/statistics')
     * 示例请求：GET /admin/session/statistics
     * 示例响应数据：
     * {
     *   "code": 200,
     *   "msg": "获取会话统计数据成功",
     *   "data": {
     *     "totalUsers": 10,
     *     "todayLogin": 100,
     *     "onlineUsers": 10,
     *     "totalSessions": 1000
     *   }
     * }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/session/statistics")
    public Result<SessionStatsDTO> getSessionStats() {
        try {
            // 调用服务层获取结构化的统计数据
            SessionStatsDTO stats = userService.getSessionStatistics();

            log.info("管理员 {} 成功获取会话统计数据：{}",
                    SecurityUtils.getCurrentUsername(), stats);
            return Result.success("获取会话统计数据成功", stats);
        } catch (Exception e) {
            log.error("获取会话统计数据失败", e);
            return Result.error("获取会话统计数据失败：" + e.getMessage());
        }
    }
}