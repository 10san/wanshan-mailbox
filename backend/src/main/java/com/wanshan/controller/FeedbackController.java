package com.wanshan.controller;

import com.wanshan.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final JdbcTemplate jdbcTemplate;

    public FeedbackController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 确保反馈表存在
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS feedbacks (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    content TEXT NOT NULL,
                    contact VARCHAR(200),
                    status TINYINT DEFAULT 0 COMMENT '0:未读 1:已读',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
        } catch (Exception ignored) {}
    }

    @PostMapping
    public Result<Void> submit(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String contact = body.getOrDefault("contact", "");

        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "反馈内容不能为空");
        }
        if (content.length() > 1000) {
            return Result.error(400, "反馈内容不能超过1000字");
        }

        jdbcTemplate.update(
                "INSERT INTO feedbacks (content, contact) VALUES (?, ?)",
                content.trim(), contact.trim());
        log.info("New feedback received, contact={}", contact);
        return Result.success();
    }
}
