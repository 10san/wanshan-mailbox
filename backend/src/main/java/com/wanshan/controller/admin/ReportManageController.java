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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class ReportManageController {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    /**
     * 举报列表
     */
    @GetMapping
    public Result<List<Report>> list(@RequestParam(defaultValue = "0") Integer status) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByAsc(Report::getStatus)  // 待处理优先
                .orderByDesc(Report::getCreatedAt);
        return Result.success(reportMapper.selectList(wrapper));
    }

    /**
     * 处理举报
     */
    @PutMapping("/{id}")
    public Result<Void> handle(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return Result.notFound("举报不存在");
        }

        String action = (String) body.get("action");
        Long handlerId = body.get("handlerId") != null
                ? Long.valueOf(body.get("handlerId").toString()) : null;

        if ("delete".equals(action)) {
            // 删除被举报内容
            if ("post".equals(report.getTargetType())) {
                Post post = postMapper.selectById(report.getTargetId());
                if (post != null) {
                    post.setStatus(3);
                    postMapper.updateById(post);
                }
            } else if ("comment".equals(report.getTargetType())) {
                Comment comment = commentMapper.selectById(report.getTargetId());
                if (comment != null) {
                    comment.setStatus(3);
                    commentMapper.updateById(comment);
                }
            }
            report.setStatus(1); // 已处理（删除）
        } else if ("dismiss".equals(action)) {
            report.setStatus(2); // 已驳回
        }

        report.setHandlerId(handlerId);
        report.setHandledAt(LocalDateTime.now());
        reportMapper.updateById(report);
        return Result.success();
    }
}
