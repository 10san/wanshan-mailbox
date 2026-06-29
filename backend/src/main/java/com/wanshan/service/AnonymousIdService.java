package com.wanshan.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 匿名身份服务 — 随机昵称 + Cookie 绑定 + 头像颜色
 */
@Service
public class AnonymousIdService {

    private static final List<String> ADJECTIVES = List.of(
            "安静的", "勇敢的", "温柔的", "孤独的", "倔强的", "迷茫的",
            "自由的", "热烈的", "慢热的", "敏感的", "快乐的", "忧郁的",
            "治愈的", "酷酷的", "暖暖的", "淡淡的", "甜甜的", "软软的",
            "呆呆的", "懒懒的", "匆匆的", "轻轻的", "深深的", "浅浅的",
            "蓝蓝的", "绿绿的", "暖暖的", "闪闪的", "远远的", "悄悄的"
    );

    private static final List<String> NOUNS = List.of(
            "考拉", "向日葵", "星辰", "大海", "山风", "月亮",
            "小熊", "狐狸", "猫咪", "鲸鱼", "蒲公英", "银杏",
            "梧桐", "云朵", "雨水", "雪花", "微风", "晚霞",
            "朝露", "森林", "溪流", "岛屿", "灯塔", "气球",
            "纸鹤", "贝壳", "珊瑚", "麋鹿", "蝴蝶", "青鸟"
    );

    private static final List<String> AVATAR_COLORS = List.of(
            "#FF7F1A", "#E0680A", "#FF9E40", "#FFBA70",
            "#6E5538", "#876B4A", "#A08464",
            "#2E86C1", "#1D76DB", "#6F42C1",
            "#0E8A16", "#D93F0B", "#C5DEF5"
    );

    private static final String COOKIE_NAME = "wanshan_uid";
    private static final Random RANDOM = new Random();

    /**
     * 获取或创建匿名身份
     */
    public AnonymousUser getOrCreate(HttpServletRequest request, HttpServletResponse response) {
        String cookieId = getCookieId(request);

        if (cookieId == null) {
            cookieId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            setCookieId(response, cookieId);
        }

        String nickname = generateNickname(cookieId);
        String avatarColor = generateColor(cookieId);

        return new AnonymousUser(cookieId, nickname, avatarColor);
    }

    /**
     * 获取 Cookie ID
     */
    public String getCookieId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setCookieId(HttpServletResponse response, String cookieId) {
        Cookie cookie = new Cookie(COOKIE_NAME, cookieId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(365 * 24 * 60 * 60); // 1 年
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private String generateNickname(String seed) {
        int hash = Math.abs(seed.hashCode());
        String adj = ADJECTIVES.get(hash % ADJECTIVES.size());
        String noun = NOUNS.get((hash / ADJECTIVES.size()) % NOUNS.size());
        return adj + noun;
    }

    private String generateColor(String seed) {
        int hash = Math.abs(seed.hashCode());
        return AVATAR_COLORS.get(hash % AVATAR_COLORS.size());
    }

    public record AnonymousUser(String cookieId, String nickname, String avatarColor) {}
}
