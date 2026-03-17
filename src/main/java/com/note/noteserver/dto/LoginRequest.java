package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "{validation.identifier.required}")
    private String identifier;

    @NotBlank(message = "{validation.password.required}")
    private String password;

    private Boolean rememberMe = false;
}
