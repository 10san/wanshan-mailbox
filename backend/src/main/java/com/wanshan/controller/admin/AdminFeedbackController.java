package com.wanshan.controller.admin;

import com.wanshan.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/feedbacks")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Result<List<Map<String, Object>>> list() {
        List<Map<String, Object>> feedbacks = jdbcTemplate.queryForList(
                "SELECT id, content, contact, status, created_at FROM feedbacks ORDER BY id DESC");
        return Result.success(feedbacks);
    }

    @PutMapping(value = "/{id}/read", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Result<Void> markRead(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE feedbacks SET status = 1 WHERE id = ?", id);
        return Result.success();
    }
}
