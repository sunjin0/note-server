package com.note.noteserver.service;

import com.note.noteserver.dto.*;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    AuthResponse login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout(String userId);

    /**
     * 刷新令牌
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * 获取当前用户信息
     */
    UserDto getCurrentUser(String userId);

    /**
     * 修改密码
     */
    void changePassword(String userId, ChangePasswordRequest request);

    /**
     * 忘记密码（通过邮箱验证码重置）
     */
    void forgotPassword(ForgotPasswordRequest request);
}
