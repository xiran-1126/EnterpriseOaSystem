package com.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class SysPost extends BaseEntity {

    private String postCode;

    private String postName;

    private Long deptId;

    private Integer orderNum;

    private String description;

    private Integer status;
}
