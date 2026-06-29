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
@TableName("posts")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private String tag;

    private String imageUrl;

    /** 删除密码的 BCrypt 哈希，可为 NULL */
    private String deleteHash;

    /** 发帖 IP 哈希（SHA-256） */
    private String ipHash;

    /** 1:正常 2:用户删除 3:管理员删除 */
    private Integer status;

    private Integer likeCount;

    private Integer hugCount;

    private Integer feelCount;

    private Integer commentCount;

    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
