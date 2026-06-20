package com.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    @Select("SELECT id FROM sys_dept WHERE parent_id = #{parentId} AND deleted = 0")
    List<Long> selectDeptIdsByParentId(@Param("parentId") Long parentId);
}
