package com.note.noteserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "{validation.refresh.token.required}")
    private String refreshToken;
}
