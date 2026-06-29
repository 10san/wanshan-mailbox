package com.wanshan.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshan.common.IpUtil;
import com.wanshan.filter.SensitiveWordFilter;
import com.wanshan.mapper.PostMapper;
import com.wanshan.model.entity.Post;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final AnonymousIdService anonymousIdService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String LIKE_KEY_PREFIX = "like:post:";
    private static final String HUG_KEY_PREFIX = "hug:post:";
    private static final String FEEL_KEY_PREFIX = "feel:post:";

    /**
     * 创建帖子
     */
    public Post createPost(String content, String tag, String deletePassword, HttpServletRequest request) {
        // 敏感词检测
        if (sensitiveWordFilter.containsSensitive(content)) {
            throw new IllegalArgumentException("内容包含不当信息，请修改后重新发布");
        }

        String ip = IpUtil.getClientIp(request);
        String ipHash = IpUtil.hashIp(ip);
        String cookieId = anonymousIdService.getCookieId(request);

        Post post = Post.builder()
                .content(content)
                .tag(tag)
                .deleteHash(deletePassword != null && !deletePassword.isEmpty()
                        ? passwordEncoder.encode(deletePassword) : null)
                .ipHash(ipHash)
                .cookieId(cookieId)
                .status(1)
                .likeCount(0)
                .hugCount(0)
                .feelCount(0)
                .commentCount(0)
                .viewCount(0)
                .build();

        postMapper.insert(post);
        log.info("Post created: id={}, tag={}", post.getId(), tag);
        return post;
    }

    /**
     * 获取帖子列表（分页）
     */
    public Page<Post> getPostList(int page, int size, String tag, String sort) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);

        if (tag != null && !tag.isEmpty() && !"all".equals(tag)) {
            wrapper.eq(Post::getTag, tag);
        }

        // 排序
        if ("hot".equals(sort)) {
            // 热度 = 点赞×2 + 评论×3 + 抱抱 + 同感
            wrapper.orderByDesc(Post::getLikeCount);
        } else {
            wrapper.orderByDesc(Post::getCreatedAt);
        }

        return postMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取帖子详情
     */
    public Post getPostDetail(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getStatus() != 1) {
            return null;
        }
        // 浏览量 +1
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);
        return post;
    }

    /**
     * 用户删除帖子（凭密码）
     */
    public boolean deleteByPassword(Long id, String password) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getStatus() != 1) return false;
        if (post.getDeleteHash() == null) return false; // 未设密码

        if (passwordEncoder.matches(password, post.getDeleteHash())) {
            post.setStatus(2);
            postMapper.updateById(post);
            return true;
        }
        return false;
    }

    /**
     * 切换点赞状态
     */
    public boolean toggleLike(Long postId, HttpServletRequest request) {
        String key = buildInteractionKey(LIKE_KEY_PREFIX, postId, request);
        return toggleInteraction(key, postId, "likeCount");
    }

    /**
     * 切换抱抱状态
     */
    public boolean toggleHug(Long postId, HttpServletRequest request) {
        String key = buildInteractionKey(HUG_KEY_PREFIX, postId, request);
        return toggleInteraction(key, postId, "hugCount");
    }

    /**
     * 切换同感状态
     */
    public boolean toggleFeel(Long postId, HttpServletRequest request) {
        String key = buildInteractionKey(FEEL_KEY_PREFIX, postId, request);
        return toggleInteraction(key, postId, "feelCount");
    }

    private String buildInteractionKey(String prefix, Long postId, HttpServletRequest request) {
        String ipHash = IpUtil.hashIp(IpUtil.getClientIp(request));
        String cookieId = anonymousIdService.getCookieId(request);
        return prefix + postId + ":" + ipHash + ":" + (cookieId != null ? cookieId : "unknown");
    }

    private boolean toggleInteraction(String key, Long postId, String field) {
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            redisTemplate.delete(key);
            decrementCount(postId, field);
            return false;
        } else {
            redisTemplate.opsForValue().set(key, "1", 7, TimeUnit.DAYS);
            incrementCount(postId, field);
            return true;
        }
    }

    private void incrementCount(Long postId, String field) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            switch (field) {
                case "likeCount" -> post.setLikeCount(post.getLikeCount() + 1);
                case "hugCount" -> post.setHugCount(post.getHugCount() + 1);
                case "feelCount" -> post.setFeelCount(post.getFeelCount() + 1);
            }
            postMapper.updateById(post);
        }
    }

    private void decrementCount(Long postId, String field) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            switch (field) {
                case "likeCount" -> post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                case "hugCount" -> post.setHugCount(Math.max(0, post.getHugCount() - 1));
                case "feelCount" -> post.setFeelCount(Math.max(0, post.getFeelCount() - 1));
            }
            postMapper.updateById(post);
        }
    }

    /**
     * 搜索帖子
     */
    public Page<Post> searchPosts(String keyword, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
                .and(w -> w.like(Post::getContent, keyword));
        wrapper.orderByDesc(Post::getCreatedAt);
        return postMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
