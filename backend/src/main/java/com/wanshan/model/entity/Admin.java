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
@TableName("admins")
public class Admin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
