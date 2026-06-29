package com.wanshan.controller;

import com.wanshan.common.PageResult;
import com.wanshan.common.Result;
import com.wanshan.model.entity.Post;
import com.wanshan.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final PostService postService;

    @GetMapping
    public Result<PageResult<Post>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (q == null || q.trim().isEmpty()) {
            return Result.success(PageResult.of(java.util.List.of(), 0, page, size));
        }

        var result = postService.searchPosts(q.trim(), page, size);
        return Result.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }
}
