package com.wanshan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshan.common.IpUtil;
import com.wanshan.filter.SensitiveWordFilter;
import com.wanshan.mapper.CommentMapper;
import com.wanshan.mapper.PostMapper;
import com.wanshan.model.entity.Comment;
import com.wanshan.model.entity.Post;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final AnonymousIdService anonymousIdService;

    @Transactional
    public Comment createComment(Long postId, String content, HttpServletRequest request) {
        if (sensitiveWordFilter.containsSensitive(content)) {
            throw new IllegalArgumentException("内容包含不当信息，请修改后重新发布");
        }

        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new IllegalArgumentException("帖子不存在");
        }

        String ipHash = IpUtil.hashIp(IpUtil.getClientIp(request));
        String cookieId = anonymousIdService.getCookieId(request);

        Comment comment = Comment.builder()
                .postId(postId)
                .content(content)
                .ipHash(ipHash)
                .cookieId(cookieId)
                .status(1)
                .likeCount(0)
                .build();

        commentMapper.insert(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        log.info("Comment created: postId={}, commentId={}", postId, comment.getId());
        return comment;
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt);
        return commentMapper.selectList(wrapper);
    }
}
