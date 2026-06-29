package com.wanshan.controller;

import com.wanshan.common.Result;
import com.wanshan.service.AnonymousIdService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommonController {

    private final AnonymousIdService anonymousIdService;

    /**
     * 获取当前匿名身份
     */
    @GetMapping("/me")
    public Result<AnonymousIdService.AnonymousUser> me(HttpServletRequest request, HttpServletResponse response) {
        return Result.success(anonymousIdService.getOrCreate(request, response));
    }

    /**
     * 获取标签列表
     */
    @GetMapping("/tags")
    public Result<List<Map<String, String>>> tags() {
        return Result.success(List.of(
                Map.of("label", "🌙 深夜emo", "value", "深夜emo"),
                Map.of("label", "💔 情感树洞", "value", "情感树洞"),
                Map.of("label", "🏠 家庭琐事", "value", "家庭琐事"),
                Map.of("label", "💼 职场压力", "value", "职场压力"),
                Map.of("label", "📚 学业烦恼", "value", "学业烦恼"),
                Map.of("label", "🌱 自我成长", "value", "自我成长"),
                Map.of("label", "💬 日常吐槽", "value", "日常吐槽"),
                Map.of("label", "🤝 人际关系", "value", "人际关系")
        ));
    }
}
