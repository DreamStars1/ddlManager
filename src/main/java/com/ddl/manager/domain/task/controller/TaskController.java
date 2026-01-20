package com.ddl.manager.domain.task.controller;

import com.ddl.manager.common.result.Result;
import com.ddl.manager.domain.statistics.annotation.ApiStatistics;
import com.ddl.manager.domain.task.dto.ProgressDTO;
import com.ddl.manager.domain.task.dto.TaskDTO;
import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.domain.task.service.TaskService;
import com.ddl.manager.infrastructure.exception.BusinessException;
import com.ddl.manager.shared.enums.TaskStatus;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务控制器（RESTful API）
 * 前后端分离接口，统一返回JSON格式数据
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Slf4j
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 获取任务列表接口
     * @param status 任务状态筛选（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页任务列表
     * 示例请求参数：
     * {
     *   "status": "TODO",
     *   "page": 0,
     *   "size": 10
     * }
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "获取任务列表成功",
     *     "data": {
     *         "content": [
     *             {
     *                 "uuid": "123456",
     *                 "title": "学习SpringBoot",
     *                 "description": "完成RESTful API学习",
     *                 "category": "学习",
     *                 "deadline": "2026-02-01T18:00:00",
     *                 "status": "TODO",
     *                 "priority": "MEDIUM",
     *                 "progress": 0,
     *                 "progressLog": "",
     *                 "createTime": "2026-01-20T10:00:00",
     *                 "updateTime": "2026-01-20T10:00:00"
     *             }
     *         ],
     *         "currentPage": 0,
     *         "totalPages": 5,
     *         "totalElements": 48
     *     }
     * }
     */
    @ApiStatistics(name = "获取任务列表", path = "/tasks")
    @GetMapping
    public Result<Map<String, Object>> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            // 分页参数：按截止时间升序排序
            Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());
            Page<TaskEntity> taskPage = taskService.getUserTasks(userId, status, pageable);

            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("content", taskPage.getContent());
            result.put("currentPage", page);
            result.put("totalPages", taskPage.getTotalPages());
            result.put("totalElements", taskPage.getTotalElements());

            return Result.success("获取任务列表成功", result);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return Result.error("获取任务列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取任务详情接口
     * @param uuid 任务UUID
     * @return 任务详情信息
     * 示例请求路径：/tasks/123456
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "获取任务详情成功",
     *     "data": {
     *         "uuid": "123456",
     *         "title": "学习SpringBoot",
     *         "description": "完成RESTful API学习",
     *         "category": "学习",
     *         "deadline": "2026-02-01T18:00:00",
     *         "status": "TODO",
     *         "priority": "MEDIUM",
     *         "progress": 0,
     *         "progressLog": "",
     *         "createTime": "2026-01-20T10:00:00",
     *         "updateTime": "2026-01-20T10:00:00"
     *     }
     * }
     */
    @ApiStatistics(name = "获取任务详情", path = "/tasks/{uuid}")
    @GetMapping("/{uuid}")
    public Result<TaskEntity> taskDetail(@PathVariable String uuid) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            TaskEntity task = taskService.getTaskByUuid(uuid, userId);
            return Result.success("获取任务详情成功", task);
        } catch (BusinessException e) {
            log.warn("获取任务详情失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取任务详情失败", e);
            return Result.error("获取任务详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建任务接口
     * @param taskDTO 任务信息
     * @param bindingResult 参数校验结果
     * @return 创建后的任务信息
     * 示例请求数据：
     * {
     *     "title": "学习SpringBoot",
     *     "description": "完成RESTful API学习",
     *     "category": "学习",
     *     "deadline": "2026-02-01T18:00",
     *     "status": "TODO",
     *     "priority": "MEDIUM",
     *     "progress": 0,
     *     "progressLog": ""
     * }
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "任务创建成功",
     *     "data": {
     *         "uuid": "123456",
     *         "title": "学习SpringBoot",
     *         "description": "完成RESTful API学习",
     *         "category": "学习",
     *         "deadline": "2026-02-01T18:00:00",
     *         "status": "TODO",
     *         "priority": "MEDIUM",
     *         "progress": 0,
     *         "progressLog": "",
     *         "createTime": "2026-01-20T10:00:00",
     *         "updateTime": "2026-01-20T10:00:00"
     *     }
     * }
     */
    @ApiStatistics(name = "创建任务", path = "/tasks")
    @PostMapping
    public Result<TaskEntity> createTask(
            @Valid @RequestBody TaskDTO taskDTO,
            BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            FieldError error = bindingResult.getFieldErrors().get(0);
            return Result.error(error.getField() + "：" + error.getDefaultMessage());
        }

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            TaskEntity task = taskService.createTask(taskDTO, userId);
            return Result.success("任务创建成功", task);
        } catch (BusinessException e) {
            log.warn("创建任务失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建任务失败", e);
            return Result.error("创建任务失败：" + e.getMessage());
        }
    }

    /**
     * 更新任务接口
     * @param uuid 任务UUID
     * @param taskDTO 任务更新信息
     * @param bindingResult 参数校验结果
     * @return 更新后的任务信息
     * 示例请求路径：/tasks/123456
     * 示例请求数据：
     * {
     *     "title": "学习SpringBoot进阶",
     *     "description": "完成RESTful API和微服务学习",
     *     "category": "学习",
     *     "deadline": "2026-02-10T18:00",
     *     "status": "IN_PROGRESS",
     *     "priority": "HIGH",
     *     "progress": 30,
     *     "progressLog": "已学习RESTful API基础"
     * }
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "任务更新成功",
     *     "data": {
     *         "uuid": "123456",
     *         "title": "学习SpringBoot进阶",
     *         "description": "完成RESTful API和微服务学习",
     *         "category": "学习",
     *         "deadline": "2026-02-10T18:00:00",
     *         "status": "IN_PROGRESS",
     *         "priority": "HIGH",
     *         "progress": 30,
     *         "progressLog": "已学习RESTful API基础",
     *         "createTime": "2026-01-20T10:00:00",
     *         "updateTime": "2026-01-21T15:30:00"
     *     }
     * }
     */
    @ApiStatistics(name = "更新任务", path = "/tasks/{uuid}")
    @PutMapping("/{uuid}")
    public Result<TaskEntity> updateTask(
            @PathVariable String uuid,
            @Valid @RequestBody TaskDTO taskDTO,
            BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            FieldError error = bindingResult.getFieldErrors().get(0);
            return Result.error(error.getField() + "：" + error.getDefaultMessage());
        }

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            TaskEntity task = taskService.updateTask(uuid, taskDTO, userId);
            return Result.success("任务更新成功", task);
        } catch (BusinessException e) {
            log.warn("更新任务失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新任务失败", e);
            return Result.error("更新任务失败：" + e.getMessage());
        }
    }

    /**
     * 删除任务接口
     * @param uuid 任务UUID
     * @return 删除结果
     * 示例请求路径：/tasks/123456
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "任务删除成功",
     *     "data": null
     * }
     */
    @ApiStatistics(name = "删除任务", path = "/tasks/{uuid}")
    @DeleteMapping("/{uuid}")
    public Result<Void> deleteTask(@PathVariable String uuid) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            taskService.deleteTask(uuid, userId);
            return Result.success("任务删除成功");
        } catch (BusinessException e) {
            log.warn("删除任务失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return Result.error("删除任务失败：" + e.getMessage());
        }
    }

    /**
     * 更新任务进度接口
     * @param uuid 任务UUID
     * @param progressDTO 进度信息
     * @param bindingResult 参数校验结果
     * @return 更新后的任务信息
     * 示例请求路径：/tasks/123456/progress
     * 示例请求数据：
     * {
     *     "progress": 60,
     *     "progressLog": "已完成RESTful API学习，开始学习微服务"
     * }
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "进度更新成功",
     *     "data": {
     *         "uuid": "123456",
     *         "title": "学习SpringBoot进阶",
     *         "description": "完成RESTful API和微服务学习",
     *         "category": "学习",
     *         "deadline": "2026-02-10T18:00:00",
     *         "status": "IN_PROGRESS",
     *         "priority": "HIGH",
     *         "progress": 60,
     *         "progressLog": "[2026-01-22T14:30:00] 已完成RESTful API学习，开始学习微服务",
     *         "createTime": "2026-01-20T10:00:00",
     *         "updateTime": "2026-01-22T14:30:00"
     *     }
     * }
     */
    @ApiStatistics(name = "更新任务进度", path = "/tasks/{uuid}/progress")
    @PatchMapping("/{uuid}/progress")
    public Result<TaskEntity> updateProgress(
            @PathVariable String uuid,
            @Valid @RequestBody ProgressDTO progressDTO,
            BindingResult bindingResult) {
        // 参数校验
        if (bindingResult.hasErrors()) {
            FieldError error = bindingResult.getFieldErrors().get(0);
            return Result.error(error.getField() + "：" + error.getDefaultMessage());
        }

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            TaskEntity task = taskService.updateProgress(
                    uuid,
                    progressDTO.getProgress(),
                    progressDTO.getProgressLog(),
                    userId);
            return Result.success("进度更新成功", task);
        } catch (BusinessException e) {
            log.warn("更新任务进度失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新任务进度失败", e);
            return Result.error("更新任务进度失败：" + e.getMessage());
        }
    }

    /**
     * 更新任务状态接口
     * @param uuid 任务UUID
     * @param status 任务状态
     * @return 更新后的任务信息
     * 示例请求路径：/tasks/123456/status?status=COMPLETED
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "状态更新成功",
     *     "data": {
     *         "uuid": "123456",
     *         "title": "学习SpringBoot进阶",
     *         "description": "完成RESTful API和微服务学习",
     *         "category": "学习",
     *         "deadline": "2026-02-10T18:00:00",
     *         "status": "COMPLETED",
     *         "priority": "HIGH",
     *         "progress": 100,
     *         "progressLog": "[2026-01-22T14:30:00] 已完成RESTful API学习，开始学习微服务\n[2026-01-25T09:15:00] 已完成所有学习内容",
     *         "createTime": "2026-01-20T10:00:00",
     *         "updateTime": "2026-01-25T09:15:00",
     *         "completedTime": "2026-01-25T09:15:00"
     *     }
     * }
     */
    @ApiStatistics(name = "更新任务状态", path = "/tasks/{uuid}/status")
    @PostMapping("/{uuid}/status")
    public Result<TaskEntity> updateStatus(
            @PathVariable String uuid,
            @RequestParam TaskStatus status) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            TaskEntity task = taskService.updateStatus(uuid, status, userId);
            return Result.success("状态更新成功", task);
        } catch (BusinessException e) {
            log.warn("更新任务状态失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新任务状态失败", e);
            return Result.error("更新任务状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取任务统计数据接口
     * @param days 即将截止天数（默认3天）
     * @return 任务统计数据
     * 示例请求路径：/tasks/statistics?days=3
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "获取任务统计数据成功",
     *     "data": {
     *         "pendingCount": 5,
     *         "inProgressCount": 3,
     *         "dueSoonCount": 2,
     *         "completedThisMonthCount": 8
     *     }
     * }
     */
    @ApiStatistics(name = "获取任务统计数据", path = "/tasks/statistics")
    @GetMapping("/statistics")
    public Result<Map<String, Integer>> getTaskStatistics(
            @RequestParam(defaultValue = "3") int days) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            Map<String, Integer> statistics = new HashMap<>();
            statistics.put("pendingCount", taskService.getPendingTaskCount(userId));
            statistics.put("inProgressCount", taskService.getInProgressCount(userId));
            statistics.put("dueSoonCount", taskService.getDueSoonCount(userId, days));
            statistics.put("completedThisMonthCount", taskService.getCompletedThisMonthCount(userId));

            return Result.success("获取任务统计数据成功", statistics);
        } catch (Exception e) {
            log.error("获取任务统计数据失败", e);
            return Result.error("获取任务统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取即将截止的任务列表
     * @param days 未来N天（默认3天）
     * @return 即将截止的任务列表
     * 示例请求路径：/tasks/upcoming?days=3
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "获取即将截止任务成功",
     *     "data": [
     *         {
     *             "uuid": "123456",
     *             "title": "学习SpringBoot",
     *             "description": "完成RESTful API学习",
     *             "category": "学习",
     *             "deadline": "2026-01-23T18:00:00",
     *             "status": "TODO",
     *             "priority": "HIGH",
     *             "progress": 0,
     *             "progressLog": "",
     *             "createTime": "2026-01-20T10:00:00",
     *             "updateTime": "2026-01-20T10:00:00"
     *         }
     *     ]
     * }
     */
    @ApiStatistics(name = "获取即将截止任务", path = "/tasks/upcoming")
    @GetMapping("/upcoming")
    public Result<java.util.List<TaskEntity>> getUpcomingTasks(
            @RequestParam(defaultValue = "3") int days) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录，请先登录");
            }

            java.util.List<TaskEntity> tasks = taskService.getUpcomingTasks(userId, days);
            return Result.success("获取即将截止任务成功", tasks);
        } catch (Exception e) {
            log.error("获取即将截止任务失败", e);
            return Result.error("获取即将截止任务失败：" + e.getMessage());
        }
    }
}