package com.note.noteserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送邮箱验证码请求
 */
@Data
public class SendEmailCodeRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    /**
     * 验证码用途，例如: "forgot_password"。
     */
    @NotBlank(message = "{validation.code.purpose.required}")
    private String purpose;
}
