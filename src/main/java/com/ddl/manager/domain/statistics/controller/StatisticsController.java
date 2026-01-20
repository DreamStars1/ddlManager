package com.ddl.manager.domain.statistics.controller;

import com.ddl.manager.common.result.Result;
import com.ddl.manager.domain.statistics.dto.ApiStatistics;
import com.ddl.manager.domain.statistics.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计数据控制器（RESTful API）
 * 前后端分离接口，统一返回JSON格式数据
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取所有API统计数据接口
     * @return 统计数据列表
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "获取统计数据成功",
     *     "data": [
     *         {
     *             "apiName": "获取任务列表",
     *             "apiPath": "/tasks",
     *             "callCount": 120,
     *             "successCount": 118,
     *             "failCount": 2,
     *             "avgResponseTime": 56.8,
     *             "lastCallTime": "2026-01-20T15:30:22"
     *         },
     *         {
     *             "apiName": "创建任务",
     *             "apiPath": "/tasks",
     *             "callCount": 85,
     *             "successCount": 85,
     *             "failCount": 0,
     *             "avgResponseTime": 89.2,
     *             "lastCallTime": "2026-01-20T14:25:10"
     *         }
     *     ]
     * }
     */
    @GetMapping
    public Result<List<ApiStatistics>> getAllStatistics() {
        try {
            List<ApiStatistics> stats = statisticsService.getAllStatistics();
            return Result.success("获取统计数据成功", stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 重置所有统计数据接口
     * @return 重置结果
     * 示例请求路径：/statistics/reset
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "统计数据重置成功！",
     *     "data": null
     * }
     */
    @PostMapping("/reset")
    public Result<Void> resetAllStatistics() {
        log.info("开始重置所有统计数据");
        try {
            // 调用Service中的重置方法
            statisticsService.resetAllStatistics();
            return Result.success("统计数据重置成功！");
        } catch (Exception e) {
            log.error("统计数据重置失败", e);
            return Result.error("统计数据重置失败：" + e.getMessage());
        }
    }
}