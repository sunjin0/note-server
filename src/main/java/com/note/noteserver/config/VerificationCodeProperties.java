package com.note.noteserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.verification.code")
public class VerificationCodeProperties {
    /**
     * 验证码有效期（秒）
     */
    private long ttlSeconds = 300;

    /**
     * 重发冷却时间（秒）
     */
    private long resendCooldownSeconds = 60;

    /**
     * 最大错误尝试次数
     */
    private int maxAttempts = 5;

    /**
     * 开发模式：true 时只打印日志，不实际发送邮件
     */
    private boolean devMode = false;

    /**
     * 开发模式通用验证码
     */
    private String devCode = "123456";
}
