package com.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {

    @Select("SELECT p.* FROM sys_post p WHERE p.dept_id = #{deptId} AND p.deleted = 0 ORDER BY p.order_num ASC")
    List<SysPost> selectPostsByDeptId(@Param("deptId") Long deptId);

    @Select("SELECT p.post_name FROM sys_post p WHERE p.id = #{postId} AND p.deleted = 0")
    String selectPostNameByPostId(@Param("postId") Long postId);
}
