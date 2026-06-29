package com.wanshan.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshan.common.Result;
import com.wanshan.mapper.IpBlockMapper;
import com.wanshan.model.entity.IpBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/ip-blocks")
@RequiredArgsConstructor
public class IpBlockController {

    private final IpBlockMapper ipBlockMapper;

    @GetMapping
    public Result<List<IpBlock>> list() {
        return Result.success(ipBlockMapper.selectList(
                new LambdaQueryWrapper<IpBlock>().orderByDesc(IpBlock::getBlockedAt)));
    }

    @PostMapping
    public Result<IpBlock> add(@RequestBody Map<String, String> body) {
        String ipHash = body.get("ipHash");
        String reason = body.get("reason");
        IpBlock block = IpBlock.builder().ipHash(ipHash).reason(reason).build();
        ipBlockMapper.insert(block);
        return Result.success(block);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ipBlockMapper.deleteById(id);
        return Result.success();
    }
}
