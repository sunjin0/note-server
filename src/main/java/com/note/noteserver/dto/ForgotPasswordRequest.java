package com.note.noteserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 忘记密码（通过邮箱验证码重置）
 */
@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.code.required}")
    private String code;

    @NotBlank(message = "{validation.new.password.required}")
    @Size(min = 6, message = "{validation.new.password.size}")
    private String newPassword;
}
