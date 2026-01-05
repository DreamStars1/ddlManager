package com.ddl.manager.domain.task.controller;

import com.ddl.manager.shared.dto.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * @author developer
 * @since 2025-12-14
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * 健康检查
     * @return 健康状态
     */
    @GetMapping("/health")
    public AjaxResult health() {
        return AjaxResult.ok("DDL管理系统运行正常");
    }

    /**
     * 系统信息
     * @return 系统信息
     */
    @GetMapping("/info")
    public AjaxResult info() {
        return AjaxResult.ok("DDL Manager v1.0.0");
    }
}
