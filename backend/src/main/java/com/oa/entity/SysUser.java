package com.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    private String password;

    private String realName;

    private String avatar;

    private String phone;

    private String email;

    private Long deptId;

    private Long postId;

    private String remark;

    private Integer status;

    private Integer loginErrorCount;

    private LocalDateTime lockTime;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;
}
