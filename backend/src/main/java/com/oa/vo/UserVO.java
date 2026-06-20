package com.oa.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private Long deptId;

    private String deptName;

    private Long postId;

    private String postName;

    private List<Long> roleIds;

    private String roleNames;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
