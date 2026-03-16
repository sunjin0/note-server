package com.note.noteserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全设置实体类
 */
@Data
@TableName("security_settings")
public class SecuritySetting {

    /**
     * 设置ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 是否密码保护
     */
    @TableField("password_protected")
    private Boolean passwordProtected;

    /**
     * 密码哈希
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 安全问题 (JSON)
     */
    @TableField("security_questions")
    private String securityQuestions;

    /**
     * 是否启用自动锁定
     */
    @TableField("auto_lock_enabled")
    private Boolean autoLockEnabled;

    /**
     * 自动锁定延迟（分钟）
     */
    @TableField("auto_lock_delay")
    private Integer autoLockDelay;

    /**
     * 是否启用生物识别
     */
    @TableField("biometric_enabled")
    private Boolean biometricEnabled;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
