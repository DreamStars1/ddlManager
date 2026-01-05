package com.ddl.manager.domain.auth.controller;

import com.ddl.manager.domain.auth.dto.RegisterDTO;
import com.ddl.manager.domain.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * 认证控制器
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Slf4j
@Controller
public class  AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录页面
     * @return 登录页面视图
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页面
     * @param model 模型
     * @return 注册页面视图
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "register";
    }

    /**
     * 处理用户注册
     * @param registerDTO 注册信息
     * @param bindingResult 校验结果
     * @param redirectAttributes 重定向属性
     * @return 重定向地址
     */
    @PostMapping("/register")
    public String register(@Valid RegisterDTO registerDTO,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        // 1. 参数校验
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    bindingResult.getFieldErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("registerDTO", registerDTO);
            return "redirect:/register";
        }

        try {
            // 2. 调用服务层注册用户
            userService.register(registerDTO);
            
            // 3. 注册成功，重定向到登录页
            redirectAttributes.addFlashAttribute("successMessage", "注册成功，请登录");
            return "redirect:/login";
        } catch (Exception e) {
            // 4. 注册失败，返回错误信息
            log.error("用户注册失败", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("registerDTO", registerDTO);
            return "redirect:/register";
        }
    }
}
