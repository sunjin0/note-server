package com.note.noteserver.service.impl;

import com.note.noteserver.dto.UpdateProfileRequest;
import com.note.noteserver.dto.UserDto;
import com.note.noteserver.entity.User;
import com.note.noteserver.exception.ServiceException;
import com.note.noteserver.mapper.UserMapper;
import com.note.noteserver.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserDto getProfile(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("error.user.not.found");
        }
        return convertToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateProfile(String userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("error.user.not.found");
        }

        boolean updated = false;
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
            updated = true;
        }
        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
            updated = true;
        }

        if (updated) {
            userMapper.updateById(user);
            log.info("User {} profile updated successfully", userId);
        }
        
        return convertToUserDto(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("error.user.not.found");
        }
        
        // 禁用用户（软删除）
        user.setIsActive(false);
        userMapper.updateById(user);
        
        // 逻辑删除
        userMapper.deleteById(userId);
        log.info("User account deleted: {}", userId);
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

