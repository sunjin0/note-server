package com.note.noteserver.service;

import com.note.noteserver.dto.UpdateProfileRequest;
import com.note.noteserver.dto.UserDto;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户资料
     */
    UserDto getProfile(String userId);

    /**
     * 更新用户资料
     */
    UserDto updateProfile(String userId, UpdateProfileRequest request);

    /**
     * 删除账户
     */
    void deleteAccount(String userId);
}
