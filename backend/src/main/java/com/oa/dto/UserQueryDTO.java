package com.oa.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserQueryDTO {

    private String username;

    private String realName;

    private String phone;

    private String email;

    private Long deptId;

    private Long postId;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String orderByColumn;

    private String isAsc = "asc";

    private Boolean showDeleted = false;

    private List<Long> deptIds;
}
