package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "{validation.current.password.required}")
    private String currentPassword;

    @NotBlank(message = "{validation.new.password.required}")
    @Size(min = 6, message = "{validation.new.password.size}")
    private String newPassword;
}
