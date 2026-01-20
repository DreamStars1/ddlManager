package com.ddl.manager.domain.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {
    // 用户名（非空校验）
    @NotBlank(message = "用户名不能为空")
    private String username;

    // 密码（非空校验）
    @NotBlank(message = "密码不能为空")
    private String password;
}