package com.wanshan.controller;

import com.wanshan.common.Result;
import com.wanshan.model.entity.Comment;
import com.wanshan.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取评论列表
     */
    @GetMapping
    public Result<List<Comment>> list(@PathVariable Long postId) {
        return Result.success(commentService.getCommentsByPostId(postId));
    }

    /**
     * 发表评论
     */
    @PostMapping
    public Result<Comment> create(@PathVariable Long postId,
                                   @RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        try {
            String content = body.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("请输入评论内容");
            }
            if (content.length() > 500) {
                return Result.error("评论内容超出字数限制（500字）");
            }

            Comment comment = commentService.createComment(postId, content.trim(), request);
            return Result.success(comment);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
