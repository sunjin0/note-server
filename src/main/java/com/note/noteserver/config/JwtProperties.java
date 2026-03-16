package com.note.noteserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥
     */
    private String secret;

    /**
     * Access Token 过期时间（毫秒）
     */
    private Long accessTokenExpiration;

    /**
     * Refresh Token 过期时间（毫秒）
     */
    private Long refreshTokenExpiration;
}
