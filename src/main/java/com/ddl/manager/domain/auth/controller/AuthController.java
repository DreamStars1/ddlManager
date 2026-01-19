package com.ddl.manager.domain.auth.controller;

import com.ddl.manager.domain.auth.dto.RegisterDTO;
import com.ddl.manager.domain.auth.dto.LoginDTO;
import com.ddl.manager.domain.auth.service.UserService;
import com.ddl.manager.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器（前后端分离版）
 * 提供登录、注册、登出的RESTful API接口
 */
@Slf4j
@RestController
@RequestMapping("/auth")  // 统一接口前缀
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * @param loginDTO 登录凭证
     * @return 登录结果（包含token等信息）
     * 示例请求数据：
     * {
     *   "username": "admin",
     *   "password": "123456"
     * }
     * 示例响应数据：
     * {
     *     "code": 200,
     *     "msg": "登录成功",
     *     "data": {
     *         "role": USER,
     *         "avatar": null,
     *         "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjM0IiwidXNlcklkIjozLCJpYXQiOjE3Njg4MzMxNDQsImV4cCI6MTc2ODkxOTU0NH0.EukMltN7xexTsmHr9iz3QthbUq-tdYPffDBhPH-BbKGy-pxObD-f7SiiCfk18EEVpW0BaVRAyGUZuHXNVBJFaA",
     *         "username": "1234"
     *     }
     * }
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            // 调用服务层完成登录逻辑，返回包含token的登录结果
            Object loginResult = userService.login(loginDTO);
            return Result.success("登录成功", loginResult);
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册接口
     * @param registerDTO 注册信息
     * @return 注册结果
     * 示例请求数据：
     * {
     *   "username": "testuser",
     *   "password": "123456",
     *   "email": "test@example.com",
     *   "avatar": "avatar.jpg"
     * }
     * 示例响应数据：
     * {
     *   "code": 200,
     *   "msg": "注册成功，请登录",
     *   "data": null
     * }
     */
    @PostMapping("/register")
    public Result<?> register(
            @Valid @ModelAttribute @RequestPart("registerDTO") RegisterDTO registerDTO,  // 接收JSON格式的表单数据
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {  // 接收头像文件（非必传）
        try {
            // 调用服务层注册用户
            userService.register(registerDTO, avatar);
            // 返回成功结果，无需重定向
            return Result.success("注册成功，请登录");
        } catch (Exception e) {
            log.error("用户注册失败", e);
            // 返回错误信息
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出接口
     * @param request 请求对象（用于获取当前登录用户信息/清理session）
     * @return 登出结果
     * 示例请求：POST /auth/logout （无需请求体）
     * 示例响应数据：
     * {
     *   "code": 200,
     *   "msg": "登出成功",
     *   "data": null
     * }
     */
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        try {
            // 调用服务层完成登出逻辑（如清理token、session等）
            userService.logout(request);
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return Result.error(e.getMessage());
        }
    }
}