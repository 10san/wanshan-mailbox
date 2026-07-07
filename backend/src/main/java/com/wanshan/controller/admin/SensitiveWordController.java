package com.wanshan.controller.admin;

import com.wanshan.common.Result;
import com.wanshan.mapper.SensitiveWordMapper;
import com.wanshan.model.entity.SensitiveWord;
import com.wanshan.filter.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sensitive-words")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Result<List<SensitiveWord>> list() {
        return Result.success(sensitiveWordMapper.selectList(null));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Result<SensitiveWord> add(@RequestBody SensitiveWord word) {
        sensitiveWordMapper.insert(word);
        sensitiveWordFilter.refreshFromDb(); // 实时生效
        return Result.success(word);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordMapper.deleteById(id);
        sensitiveWordFilter.refreshFromDb();
        return Result.success();
    }
}
