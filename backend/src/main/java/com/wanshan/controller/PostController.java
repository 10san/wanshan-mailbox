package com.wanshan.controller;

import com.wanshan.common.PageResult;
import com.wanshan.common.Result;
import com.wanshan.model.entity.Post;
import com.wanshan.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 获取帖子列表
     */
    @GetMapping
    public Result<PageResult<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "latest") String sort) {

        var result = postService.getPostList(page, size, tag, sort);
        return Result.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public Result<Post> detail(@PathVariable Long id) {
        Post post = postService.getPostDetail(id);
        if (post == null) {
            return Result.notFound("这条心事已经消失了 🌙");
        }
        return Result.success(post);
    }

    /**
     * 创建帖子
     */
    @PostMapping
    public Result<Post> create(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String content = body.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("请输入内容");
            }
            if (content.length() > 2000) {
                return Result.error("内容超出字数限制（2000字）");
            }

            Post post = postService.createPost(
                    content.trim(),
                    body.get("tag"),
                    body.get("deletePassword"),
                    request
            );
            return Result.success(post);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除帖子（凭密码）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.isEmpty()) {
            return Result.error("请输入删除密码");
        }
        boolean ok = postService.deleteByPassword(id, password);
        if (ok) {
            return Result.success();
        }
        return Result.error("密码错误或帖子未设置删除密码");
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{id}/like")
    public Result<Boolean> like(@PathVariable Long id, HttpServletRequest request) {
        boolean liked = postService.toggleLike(id, request);
        return Result.success(liked);
    }

    /**
     * 抱抱
     */
    @PostMapping("/{id}/hug")
    public Result<Boolean> hug(@PathVariable Long id, HttpServletRequest request) {
        boolean hugged = postService.toggleHug(id, request);
        return Result.success(hugged);
    }

    /**
     * 同感
     */
    @PostMapping("/{id}/feel")
    public Result<Boolean> feel(@PathVariable Long id, HttpServletRequest request) {
        boolean felt = postService.toggleFeel(id, request);
        return Result.success(felt);
    }
}
