package com.wanshan.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshan.common.PageResult;
import com.wanshan.common.Result;
import com.wanshan.mapper.CommentMapper;
import com.wanshan.mapper.PostMapper;
import com.wanshan.model.entity.Comment;
import com.wanshan.model.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/posts")
@RequiredArgsConstructor
public class PostManageController {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    /**
     * 帖子列表（管理后台）
     */
    @GetMapping
    public Result<PageResult<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Post::getContent, keyword);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        post.setStatus(3); // 管理员删除
        postMapper.updateById(post);
        return Result.success();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.notFound("评论不存在");
        }
        comment.setStatus(3);
        commentMapper.updateById(comment);
        return Result.success();
    }
}
