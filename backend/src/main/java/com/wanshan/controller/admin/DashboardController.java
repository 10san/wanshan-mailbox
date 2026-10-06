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

        // --- 今日统计（全部） ---
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

        // --- 真实用户统计 ---
        long todayRealPosts = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .ge(Post::getCreatedAt, todayStart)
                        .lt(Post::getCreatedAt, tomorrowStart)
                        .eq(Post::getIsBot, 0));
        long todayRealComments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .ge(Comment::getCreatedAt, todayStart)
                        .lt(Comment::getCreatedAt, tomorrowStart)
                        .eq(Comment::getIsBot, 0));
        long totalRealPosts = postMapper.selectCount(
                new LambdaQueryWrapper<Post>().eq(Post::getIsBot, 0));

        // --- 机器人统计 ---
        long todayBotPosts = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .ge(Post::getCreatedAt, todayStart)
                        .lt(Post::getCreatedAt, tomorrowStart)
                        .eq(Post::getIsBot, 1));
        long todayBotComments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .ge(Comment::getCreatedAt, todayStart)
                        .lt(Comment::getCreatedAt, tomorrowStart)
                        .eq(Comment::getIsBot, 1));
        long totalBotPosts = postMapper.selectCount(
                new LambdaQueryWrapper<Post>().eq(Post::getIsBot, 1));

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
            long dayRealPosts = postMapper.selectCount(
                    new LambdaQueryWrapper<Post>()
                            .ge(Post::getCreatedAt, dayStart)
                            .lt(Post::getCreatedAt, dayEnd)
                            .eq(Post::getIsBot, 0));

            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.toString());
            day.put("label", (date.getMonthValue()) + "/" + date.getDayOfMonth());
            day.put("posts", dayPosts);
            day.put("comments", dayComments);
            day.put("realPosts", dayRealPosts);
            trend.add(day);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("todayPosts", todayPosts);
        result.put("todayComments", todayComments);
        result.put("totalPosts", totalPosts);
        result.put("pendingReports", pendingReports);
        result.put("totalViews", totalViews != null ? totalViews : 0L);
        result.put("todayRealPosts", todayRealPosts);
        result.put("todayRealComments", todayRealComments);
        result.put("totalRealPosts", totalRealPosts);
        result.put("todayBotPosts", todayBotPosts);
        result.put("todayBotComments", todayBotComments);
        result.put("totalBotPosts", totalBotPosts);
        result.put("trend", trend);
        return Result.success(result);
    }
}
