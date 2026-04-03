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
    factors JSON,
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