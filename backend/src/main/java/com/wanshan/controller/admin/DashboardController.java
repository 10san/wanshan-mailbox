package com.wanshan.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshan.common.Result;
import com.wanshan.mapper.CommentMapper;
import com.wanshan.mapper.PostMapper;
import com.wanshan.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;

    @GetMapping
    public Result<Map<String, Object>> stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long todayPosts = postMapper.selectCount(
                new LambdaQueryWrapper<com.wanshan.model.entity.Post>()
                        .ge(com.wanshan.model.entity.Post::getCreatedAt, todayStart));
        long todayComments = commentMapper.selectCount(
                new LambdaQueryWrapper<com.wanshan.model.entity.Comment>()
                        .ge(com.wanshan.model.entity.Comment::getCreatedAt, todayStart));
        long totalPosts = postMapper.selectCount(null);
        long pendingReports = reportMapper.selectCount(
                new LambdaQueryWrapper<com.wanshan.model.entity.Report>()
                        .eq(com.wanshan.model.entity.Report::getStatus, 0));

        return Result.success(Map.of(
                "todayPosts", todayPosts,
                "todayComments", todayComments,
                "totalPosts", totalPosts,
                "pendingReports", pendingReports
        ));
    }
}
