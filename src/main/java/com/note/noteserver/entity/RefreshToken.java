package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷新令牌实体类
 */
@Data
@TableName("refresh_tokens")
public class RefreshToken {

    /**
     * 令牌ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 令牌哈希
     */
    @TableField("token_hash")
    private String tokenHash;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备信息
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 过期时间
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 吊销时间
     */
    @TableField("revoked_at")
    private LocalDateTime revokedAt;

    /**
     * 是否吊销
     */
    @TableField("is_revoked")
    private Boolean isRevoked;
}
