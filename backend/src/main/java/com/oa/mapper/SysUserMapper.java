package com.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.dto.UserQueryDTO;
import com.oa.entity.SysUser;
import com.oa.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT d.dept_name FROM sys_dept d WHERE d.id = #{deptId} AND d.deleted = 0")
    String selectDeptNameByDeptId(@Param("deptId") Long deptId);

    @Select("SELECT r.id FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT p.post_name FROM sys_post p WHERE p.id = #{postId} AND p.deleted = 0")
    String selectPostNameByPostId(@Param("postId") Long postId);

    IPage<UserVO> selectUserPage(Page<UserVO> page, @Param("query") UserQueryDTO query);

    List<UserVO> selectUserList(@Param("query") UserQueryDTO query);
}
