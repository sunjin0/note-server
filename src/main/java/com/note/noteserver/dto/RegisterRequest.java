package com.note.noteserver.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户注册请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "{validation.username.required}")
    @Size(min = 3, max = 20, message = "{validation.username.size}")
    private String username;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, message = "{validation.password.size}")
    private String password;

    @Size(max = 50, message = "{validation.nickname.size}")
    private String nickname;
}
