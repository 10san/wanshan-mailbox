package com.wanshan.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ip_blocks")
public class IpBlock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ipHash;

    private String reason;

    private Long blockedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime blockedAt;
}
