package com.ddl.manager.domain.task.controller;

import com.ddl.manager.domain.task.service.TaskService;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 主页仪表盘控制器
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@Controller
public class DashboardController {

    @Autowired
    private TaskService taskService;

    /**
     * 主页 - 添加真实任务数量统计
     * @param model 模型
     * @return 主页视图
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return "redirect:/login";
            }

            // 获取真实的任务数量
            int pendingCount = taskService.getPendingTaskCount(userId);
            int inProgressCount = taskService.getInProgressCount(userId);
            int dueSoonCount = taskService.getDueSoonCount(userId, 3); // 3天内截止
            int completedThisMonthCount = taskService.getCompletedThisMonthCount(userId);

            // 添加到模型
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("inProgressCount", inProgressCount);
            model.addAttribute("dueSoonCount", dueSoonCount);
            model.addAttribute("completedThisMonthCount", completedThisMonthCount);
            model.addAttribute("message", "欢迎使用DDL管理系统");

            log.debug("用户 {} 的仪表盘数据: 待办={}, 进行中={}, 即将截止={}, 本月完成={}",
                    userId, pendingCount, inProgressCount, dueSoonCount, completedThisMonthCount);

            return "dashboard"; // 根据你的HTML文件名，可能是 "index" 或 "dashboard"
        } catch (Exception e) {
            log.error("加载仪表盘数据失败", e);
            // 出错时设置默认值
            model.addAttribute("pendingCount", 0);
            model.addAttribute("inProgressCount", 0);
            model.addAttribute("dueSoonCount", 0);
            model.addAttribute("completedThisMonthCount", 0);
            model.addAttribute("message", "欢迎使用DDL管理系统");
            return "dashboard";
        }
    }
}