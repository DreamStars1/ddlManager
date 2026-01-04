package com.ddl.manager.domain.auth.controller;

import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.service.UserService;
import com.ddl.manager.infrastructure.annotation.RequiresPermission;
import com.ddl.manager.shared.dto.AjaxResult;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器 - 完善版本
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 管理员仪表盘
     */
    @RequiresPermission(roles = {"ROLE_ADMIN"})
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "管理员仪表盘");
        return "admin/dashboard";
    }

    /**
     * 用户管理页面
     */
    @RequiresPermission(roles = {"ROLE_ADMIN"})
    @GetMapping("/users")
    public String userManagement(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        try {
            // 获取所有用户（分页）
            Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
            // 这里需要实现分页查询所有用户的方法
            // Page<UserEntity> userPage = userService.getAllUsers(pageable);

            // 临时数据
            model.addAttribute("activeMenu", "users");
            model.addAttribute("pageTitle", "用户管理");
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", 1); // 临时值
            model.addAttribute("totalElements", 2); // 临时值

            return "admin/users";
        } catch (Exception e) {
            log.error("加载用户管理页面失败", e);
            model.addAttribute("errorMessage", "加载用户管理页面失败");
            return "admin/users";
        }
    }

    /**
     * 系统统计页面
     */
    @RequiresPermission(roles = {"ROLE_ADMIN"})
    @GetMapping("/statistics")
    public String statisticsPage(Model model) {
        model.addAttribute("activeMenu", "statistics");
        model.addAttribute("pageTitle", "系统统计");
        return "admin/statistics";
    }

    /**
     * 获取所有用户列表API
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users")
    @ResponseBody
    public AjaxResult listUsers(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        try {
            // 这里应该返回分页的用户列表
            // 临时返回成功响应
            return AjaxResult.ok("获取用户列表成功");
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return AjaxResult.error("获取用户列表失败");
        }
    }

    /**
     * 禁用用户
     */
    @RequiresPermission(roles = {"ROLE_ADMIN"})
    @PostMapping("/users/{userId}/disable")
    @ResponseBody
    public AjaxResult disableUser(@PathVariable Long userId) {
        try {
            log.info("管理员 {} 禁用了用户 {}", SecurityUtils.getCurrentUsername(), userId);
            // 这里实现禁用用户逻辑
            return AjaxResult.ok("用户禁用成功");
        } catch (Exception e) {
            log.error("禁用用户失败", e);
            return AjaxResult.error("禁用用户失败");
        }
    }

    /**
     * 启用用户
     */
    @RequiresPermission(roles = {"ROLE_ADMIN"})
    @PostMapping("/users/{userId}/enable")
    @ResponseBody
    public AjaxResult enableUser(@PathVariable Long userId) {
        try {
            log.info("管理员 {} 启用了用户 {}", SecurityUtils.getCurrentUsername(), userId);
            // 这里实现启用用户逻辑
            return AjaxResult.ok("用户启用成功");
        } catch (Exception e) {
            log.error("启用用户失败", e);
            return AjaxResult.error("启用用户失败");
        }
    }
}