package com.wanshan.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshan.common.Result;
import com.wanshan.mapper.CommentMapper;
import com.wanshan.mapper.PostMapper;
import com.wanshan.mapper.ReportMapper;
import com.wanshan.model.entity.Comment;
import com.wanshan.model.entity.Post;
import com.wanshan.model.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Result<Map<String, Object>> stats() {
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDateTime todayStart = LocalDate.now(zone).atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        long todayPosts = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .ge(Post::getCreatedAt, todayStart)
                        .lt(Post::getCreatedAt, tomorrowStart));
        long todayComments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .ge(Comment::getCreatedAt, todayStart)
                        .lt(Comment::getCreatedAt, tomorrowStart));
        long totalPosts = postMapper.selectCount(null);
        long pendingReports = reportMapper.selectCount(
                new LambdaQueryWrapper<Report>()
                        .eq(Report::getStatus, 0));

        // 累计浏览量
        Long totalViews = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(view_count), 0) FROM posts", Long.class);

        // 近7天趋势（真实数据）
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now(zone).minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            long dayPosts = postMapper.selectCount(
                    new LambdaQueryWrapper<Post>()
                            .ge(Post::getCreatedAt, dayStart)
                            .lt(Post::getCreatedAt, dayEnd));
            long dayComments = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>()
                            .ge(Comment::getCreatedAt, dayStart)
                            .lt(Comment::getCreatedAt, dayEnd));

            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.toString());
            day.put("label", (date.getMonthValue()) + "/" + date.getDayOfMonth());
            day.put("posts", dayPosts);
            day.put("comments", dayComments);
            trend.add(day);
        }

        return Result.success(Map.of(
                "todayPosts", todayPosts,
                "todayComments", todayComments,
                "totalPosts", totalPosts,
                "pendingReports", pendingReports,
                "totalViews", totalViews != null ? totalViews : 0L,
                "trend", trend
        ));
    }
}
