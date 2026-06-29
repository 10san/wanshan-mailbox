-- ============================================
-- 晚山信箱 V1 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS wanshan_mailbox
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE wanshan_mailbox;

-- 帖子表
CREATE TABLE IF NOT EXISTS posts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    content       TEXT NOT NULL,
    tag           VARCHAR(50),
    image_url     VARCHAR(500),
    delete_hash   VARCHAR(255) COMMENT '删除密码 BCrypt 哈希，可为 NULL',
    ip_hash       VARCHAR(64) COMMENT '发帖 IP 的 SHA-256 哈希',
    cookie_id     VARCHAR(64) COMMENT '匿名用户 Cookie 标识',
    status        TINYINT DEFAULT 1 COMMENT '1:正常 2:用户删除 3:管理员删除',
    like_count    INT DEFAULT 0,
    hug_count     INT DEFAULT 0,
    feel_count    INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    view_count    INT DEFAULT 0,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FULLTEXT INDEX ft_content (content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT NOT NULL,
    content     VARCHAR(500) NOT NULL,
    ip_hash     VARCHAR(64),
    cookie_id   VARCHAR(64),
    status      TINYINT DEFAULT 1 COMMENT '1:正常 2:用户删除 3:管理员删除',
    like_count  INT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 举报表
CREATE TABLE IF NOT EXISTS reports (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL COMMENT 'post / comment',
    target_id   BIGINT NOT NULL,
    reason      VARCHAR(100) NOT NULL,
    ip_hash     VARCHAR(64),
    status      TINYINT DEFAULT 0 COMMENT '0:待处理 1:已处理(删除) 2:已驳回',
    handler_id  BIGINT,
    handled_at  DATETIME,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 管理员表
CREATE TABLE IF NOT EXISTS admins (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt 哈希',
    role        VARCHAR(20) DEFAULT 'admin',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化默认管理员 (密码: admin123)
INSERT IGNORE INTO admins (username, password, role)
VALUES ('admin', '$2b$12$QyfQBEXkUwYPJHHCPVJCOuNVlXerVUrrSbuhKklP1L/R36Xd6DtTe', 'admin');

-- 敏感词表
CREATE TABLE IF NOT EXISTS sensitive_words (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    word        VARCHAR(100) NOT NULL UNIQUE,
    match_type  TINYINT DEFAULT 1 COMMENT '1:精确匹配 2:正则匹配',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 预置基础敏感词
INSERT IGNORE INTO sensitive_words (word, match_type) VALUES
('赌博', 1), ('彩票', 1), ('六合彩', 1),
('色情', 1), ('裸聊', 1), ('约炮', 1),
('毒品', 1), ('冰毒', 1), ('大麻', 1),
('枪支', 1), ('炸药', 1),
('广告', 1), ('加微信', 1), ('加QQ', 1), ('微信号', 1), ('QQ号', 1),
('兼职', 1), ('刷单', 1), ('返利', 1),
('代购', 1), ('微商', 1), ('代理', 1);

-- IP 封禁表
CREATE TABLE IF NOT EXISTS ip_blocks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_hash     VARCHAR(64) NOT NULL UNIQUE,
    reason      VARCHAR(200),
    blocked_by  BIGINT,
    blocked_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 埋点事件表（可选）
CREATE TABLE IF NOT EXISTS analytics_events (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_name  VARCHAR(50) NOT NULL,
    page_name   VARCHAR(50),
    properties  JSON,
    ip_hash     VARCHAR(64),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_name (event_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
