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
@TableName("reports")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** post / comment */
    private String targetType;

    private Long targetId;

    private String reason;

    private String ipHash;

    /** 0:待处理 1:已处理(删除) 2:已驳回 */
    private Integer status;

    private Long handlerId;

    private LocalDateTime handledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
