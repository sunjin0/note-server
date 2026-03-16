-- =====================================================
-- Mood Journal 数据库结构脚本 (MySQL 8.0+)
-- 编码: UTF-8mb4
-- 创建日期: 2024-01-15
-- =====================================================

-- 创建数据库（如需要）
-- CREATE DATABASE IF NOT EXISTS mood_journal 
--     CHARACTER SET utf8mb4 
--     COLLATE utf8mb4_unicode_ci;
-- USE mood_journal;

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 用户表 (users)
-- =====================================================
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    
    -- 约束
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3 AND LENGTH(username) <= 20),
    CONSTRAINT chk_email_format CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户基本信息表';

-- 索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_created_at ON users(created_at);

-- =====================================================
-- 2. 刷新令牌表 (refresh_tokens)
-- =====================================================
DROP TABLE IF EXISTS refresh_tokens;

CREATE TABLE refresh_tokens (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    device_id VARCHAR(100),
    device_info TEXT,
    ip_address VARCHAR(45),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP NULL,
    is_revoked BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='JWT刷新令牌表';

-- 索引
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- =====================================================
-- 3. 日记条目表 (mood_entries)
-- =====================================================
DROP TABLE IF EXISTS mood_entries;

CREATE TABLE mood_entries (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    entry_date DATE NOT NULL,
    mood_type VARCHAR(10) NOT NULL,
    journal_content TEXT,
    journal_encrypted BOOLEAN DEFAULT FALSE,
    photos JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    sync_version INT DEFAULT 1,
    
    -- 约束
    CONSTRAINT chk_mood_type CHECK (mood_type IN ('great', 'good', 'okay', 'sad', 'angry')),
    UNIQUE KEY uk_user_entry_date (user_id, entry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户日记条目表';

-- 索引
CREATE INDEX idx_mood_entries_user_id ON mood_entries(user_id);
CREATE INDEX idx_mood_entries_entry_date ON mood_entries(entry_date);
CREATE INDEX idx_mood_entries_user_date ON mood_entries(user_id, entry_date);
CREATE INDEX idx_mood_entries_mood_type ON mood_entries(mood_type);
CREATE INDEX idx_mood_entries_updated_at ON mood_entries(updated_at);

-- =====================================================
-- 4. 影响因素选项表 (factor_options)
-- =====================================================
DROP TABLE IF EXISTS factor_options;

CREATE TABLE factor_options (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NULL,  -- NULL表示系统预设
    label VARCHAR(50) NOT NULL,
    emoji VARCHAR(10) NOT NULL,
    is_custom BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    -- 外键约束已移除
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='心情影响因素选项表';

-- 索引
CREATE INDEX idx_factor_options_user_id ON factor_options(user_id);
CREATE INDEX idx_factor_options_is_custom ON factor_options(is_custom);
CREATE INDEX idx_factor_options_is_active ON factor_options(is_active);

-- =====================================================
-- 5. 日记条目与影响因素关联表 (entry_factors)
-- =====================================================
DROP TABLE IF EXISTS entry_factors;

CREATE TABLE entry_factors (
    id CHAR(36) PRIMARY KEY,
    entry_id CHAR(36) NOT NULL,
    factor_id CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束已移除
    UNIQUE KEY uk_entry_factor (entry_id, factor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='日记条目与影响因素多对多关联表';

-- 索引
CREATE INDEX idx_entry_factors_entry_id ON entry_factors(entry_id);
CREATE INDEX idx_entry_factors_factor_id ON entry_factors(factor_id);

-- =====================================================
-- 6. 日记模板表 (journal_templates)
-- =====================================================
DROP TABLE IF EXISTS journal_templates;

CREATE TABLE journal_templates (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NULL,  -- NULL表示系统预设
    category VARCHAR(20) NOT NULL,
    title_key VARCHAR(100) NOT NULL,
    content_key VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    content TEXT,
    is_custom BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    usage_count INT DEFAULT 0,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 外键约束已移除
    CONSTRAINT chk_template_category CHECK (
        category IN ('work', 'study', 'travel', 'health', 'life', 'custom')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='日记模板表';

-- 索引
CREATE INDEX idx_journal_templates_user_id ON journal_templates(user_id);
CREATE INDEX idx_journal_templates_category ON journal_templates(category);
CREATE INDEX idx_journal_templates_is_custom ON journal_templates(is_custom);

-- =====================================================
-- 7. 用户设置表 (user_settings)
-- =====================================================
DROP TABLE IF EXISTS user_settings;

CREATE TABLE user_settings (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL UNIQUE,
    settings_data JSON NOT NULL,
    encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户应用设置表';

-- 索引
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);

-- =====================================================
-- 8. 安全设置表 (security_settings)
-- =====================================================
DROP TABLE IF EXISTS security_settings;

CREATE TABLE security_settings (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL UNIQUE,
    password_protected BOOLEAN DEFAULT FALSE,
    password_hash VARCHAR(255),
    security_questions JSON,
    auto_lock_enabled BOOLEAN DEFAULT FALSE,
    auto_lock_delay INT DEFAULT 5,  -- 分钟
    biometric_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户安全设置表';

-- 索引
CREATE INDEX idx_security_settings_user_id ON security_settings(user_id);

-- =====================================================
-- 9. 同步状态表 (sync_status)
-- =====================================================
DROP TABLE IF EXISTS sync_status;

CREATE TABLE sync_status (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    device_name VARCHAR(100),
    last_sync_at TIMESTAMP NULL,
    last_sync_result JSON,
    pending_changes INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 外键约束已移除
    UNIQUE KEY uk_user_device (user_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='设备同步状态表';

-- 索引
CREATE INDEX idx_sync_status_user_id ON sync_status(user_id);
CREATE INDEX idx_sync_status_device_id ON sync_status(device_id);
CREATE INDEX idx_sync_status_last_sync ON sync_status(last_sync_at);

-- =====================================================
-- 10. 同步日志表 (sync_logs)
-- =====================================================
DROP TABLE IF EXISTS sync_logs;

CREATE TABLE sync_logs (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    sync_type VARCHAR(20) NOT NULL,  -- push/pull/full
    status VARCHAR(20) NOT NULL,     -- success/error/conflict
    entries_uploaded INT DEFAULT 0,
    entries_downloaded INT DEFAULT 0,
    conflicts_count INT DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='同步操作日志表';

-- 索引
CREATE INDEX idx_sync_logs_user_id ON sync_logs(user_id);
CREATE INDEX idx_sync_logs_device_id ON sync_logs(device_id);
CREATE INDEX idx_sync_logs_created_at ON sync_logs(started_at);

-- 外键检查保持关闭
SET FOREIGN_KEY_CHECKS = 0;
