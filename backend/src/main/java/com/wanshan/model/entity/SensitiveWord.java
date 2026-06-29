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
@TableName("sensitive_words")
public class SensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String word;

    /** 1:精确匹配 2:正则匹配 */
    private Integer matchType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
