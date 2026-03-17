package com.note.noteserver.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "{validation.nickname.size}")
    private String nickname;

    private String avatar;
}
