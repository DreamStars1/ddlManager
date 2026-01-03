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

/**
 * 管理员控制器
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@Controller
@RequestMapping("/admin")
// 类级别权限控制
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 用户管理页面
     */
    @RequiresPermission(roles = {"ROLE_ADMIN"})
    @GetMapping("/users")
    public String userManagement(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        // 这里简单演示，实际应该实现分页查询所有用户
        model.addAttribute("message", "用户管理功能开发中...");
        return "admin/users";
    }

    /**
     * 获取用户列表API - 需要ADMIN角色
     */
    @PreAuthorize("hasRole('ADMIN')") // Spring Security原生注解
    @GetMapping("/api/users")
    @ResponseBody
    public AjaxResult listUsers(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        try {
            // 模拟用户数据
            return AjaxResult.ok("获取用户列表成功");
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return AjaxResult.error("获取用户列表失败");
        }
    }

    /**
     * 禁用用户 - 需要ADMIN角色
     */
    @RequiresPermission(roles = {"ROLE_ADMIN"})
    @PostMapping("/users/{userId}/disable")
    @ResponseBody
    public AjaxResult disableUser(@PathVariable Long userId) {
        try {
            log.info("管理员 {} 禁用了用户 {}", SecurityUtils.getCurrentUsername(), userId);
            return AjaxResult.ok("用户禁用成功");
        } catch (Exception e) {
            log.error("禁用用户失败", e);
            return AjaxResult.error("禁用用户失败");
        }
    }
}