package com.wanshan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanshan.model.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT * FROM posts WHERE status = 1 AND MATCH(content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) ORDER BY created_at DESC")
    List<Post> searchByKeyword(@Param("keyword") String keyword);
}
