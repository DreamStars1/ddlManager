package com.ddl.manager.domain.task.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 主页仪表盘控制器
 * @author developer
 * @since 2025-12-14
 */
@Controller
public class DashboardController {

    /**
     * 主页
     * @param model 模型
     * @return 主页视图
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("message", "欢迎使用DDL管理系统");
        return "dashboard";
    }
}
