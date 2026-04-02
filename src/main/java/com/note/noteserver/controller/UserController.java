package com.note.noteserver.controller;

import com.note.noteserver.dto.ApiResponse;
import com.note.noteserver.dto.UpdateProfileRequest;
import com.note.noteserver.dto.UserDto;
import com.note.noteserver.service.UserService;
import com.note.noteserver.util.I18nMessageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户资料控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(
            @RequestAttribute("userId") String userId) {
        UserDto user = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserDto user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 删除账户
     */
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteAccount(
            @RequestAttribute("userId") String userId) {
        userService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", I18nMessageUtil.getMessage("success.account.deleted"))));
    }
}
