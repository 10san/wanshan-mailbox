package com.wanshan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanshan.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
