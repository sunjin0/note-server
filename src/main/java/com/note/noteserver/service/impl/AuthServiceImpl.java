package com.note.noteserver.service.impl;

import com.note.noteserver.dto.*;
import com.note.noteserver.entity.RefreshToken;
import com.note.noteserver.entity.User;
import com.note.noteserver.exception.I18nException;
import com.note.noteserver.mapper.RefreshTokenMapper;
import com.note.noteserver.mapper.UserMapper;
import com.note.noteserver.service.AuthService;
import com.note.noteserver.util.JwtUtil;
import com.note.noteserver.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("用户注册: {}", request.getUsername());
        
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new I18nException("error.user.username.exists");
        }
        
        // 检查邮箱是否已存在
        User existingEmail = userMapper.findByEmail(request.getEmail());
        if (existingEmail != null) {
            throw new I18nException("error.user.email.exists");
        }
        // 创建用户
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setIsActive(true);
        user.setEmailVerified(false);
        
        userMapper.insert(user);
        
        // 生成令牌
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("用户登录: {}", request.getIdentifier());
        
        // 根据邮箱或用户名查找用户
        User user = userMapper.findByEmail(request.getIdentifier());
        if (user == null) {
            user = userMapper.findByUsername(request.getIdentifier());
        }
        
        if (user == null) {
            throw new I18nException("error.user.not.found");
        }
        
        // 验证密码
        if (!passwordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new I18nException("error.user.invalid.credentials");
        }
        
        // 检查用户是否激活
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new I18nException("error.user.disabled");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 生成令牌
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String userId) {
        log.info("用户登出: {}", userId);
        
        // 吊销该用户的所有刷新令牌
        List<RefreshToken> tokens = refreshTokenMapper.findByUserId(userId);
        for (RefreshToken token : tokens) {
            token.setIsRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenMapper.updateById(token);
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("刷新令牌");
        
        String token = request.getRefreshToken();
        
        // 验证 Refresh Token
        if (!jwtUtil.validateToken(token)) {
            throw new I18nException("error.auth.invalid.refresh.token");
        }
        
        // 验证是否为 Refresh Token
        if (!jwtUtil.isRefreshToken(token)) {
            throw new I18nException("error.auth.invalid.token.type");
        }
        
        // 提取用户ID
        String userId = jwtUtil.extractUserId(token);
        
        // 查找数据库中的令牌
        RefreshToken refreshToken = refreshTokenMapper.findByTokenHash(token);
        if (refreshToken == null || Boolean.TRUE.equals(refreshToken.getIsRevoked())) {
            throw new I18nException("error.auth.refresh.token.revoked");
        }
        
        // 检查是否过期
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new I18nException("error.auth.refresh.token.expired");
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new I18nException("error.user.not.found");
        }
        
        // 吊销旧令牌
        refreshToken.setIsRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenMapper.updateById(refreshToken);
        
        // 生成新令牌
        return generateAuthResponse(user);
    }

    @Override
    public UserDto getCurrentUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new I18nException("error.user.not.found");
        }
        return convertToUserDto(user);
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new I18nException("error.user.not.found");
        }
        
        // 验证当前密码
        if (!passwordUtil.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new I18nException("error.user.current.password.wrong");
        }
        
        // 加密新密码
        user.setPasswordHash(passwordUtil.encode(request.getNewPassword()));
        userMapper.updateById(user);
        
        // 吊销所有刷新令牌（强制重新登录）
        logout(userId);
        
        log.info("User {} changed password successfully", userId);
    }

    /**
     * 生成认证响应（包含 JWT 令牌）
     */
    private AuthResponse generateAuthResponse(User user) {
        // 生成 Access Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        
        // 生成 Refresh Token
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        // 保存 Refresh Token 到数据库
        saveRefreshToken(user.getId(), refreshToken);
        
        // 获取过期时间
        Date expirationDate = jwtUtil.getExpirationDate(accessToken);
        LocalDateTime expiresAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        return AuthResponse.builder()
                .user(convertToUserDto(user))
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * 保存 Refresh Token
     */
    private void saveRefreshToken(String userId, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID().toString());
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(token);
        refreshToken.setIsRevoked(false);
        
        // 设置过期时间（7天）
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        refreshTokenMapper.insert(refreshToken);
    }

    /**
     * 转换为 UserDto
     */
    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
