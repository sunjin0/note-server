package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "邮箱或用户名不能为空")
    private String identifier;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe = false;
}
