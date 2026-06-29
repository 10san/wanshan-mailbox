package com.wanshan.controller.admin;

import com.wanshan.common.Result;
import com.wanshan.mapper.SensitiveWordMapper;
import com.wanshan.model.entity.SensitiveWord;
import com.wanshan.filter.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sensitive-words")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    @GetMapping
    public Result<List<SensitiveWord>> list() {
        return Result.success(sensitiveWordMapper.selectList(null));
    }

    @PostMapping
    public Result<SensitiveWord> add(@RequestBody SensitiveWord word) {
        sensitiveWordMapper.insert(word);
        sensitiveWordFilter.refresh(); // 实时生效
        return Result.success(word);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordMapper.deleteById(id);
        sensitiveWordFilter.refresh();
        return Result.success();
    }
}
