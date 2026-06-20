package com.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String ipAddr;

    private String loginLocation;

    private String browser;

    private String os;

    private Integer status;

    private String msg;

    private LocalDateTime loginTime;

    private Integer abnormal;

    private String deviceType;

    private String token;
}
