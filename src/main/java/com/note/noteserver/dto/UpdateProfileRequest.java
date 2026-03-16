package com.note.noteserver.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "昵称长度不能超过50字符")
    private String nickname;

    private String avatar;
}
