-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名/工号',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    email VARCHAR(128) DEFAULT '' COMMENT '邮箱',
    dept_id BIGINT DEFAULT NULL COMMENT '部门ID',
    post_id BIGINT DEFAULT NULL COMMENT '岗位ID',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    login_error_count INT DEFAULT 0 COMMENT '登录错误次数',
    lock_time DATETIME DEFAULT NULL COMMENT '锁定时间',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) DEFAULT '' COMMENT '最后登录IP',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除'
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(255) DEFAULT '' COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除'
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(64) NOT NULL COMMENT '部门名称',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(64) DEFAULT '' COMMENT '负责人',
    phone VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    email VARCHAR(128) DEFAULT '' COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除'
);

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) DEFAULT '' COMMENT '用户名',
    ip_addr VARCHAR(64) DEFAULT '' COMMENT '登录IP',
    login_location VARCHAR(255) DEFAULT '' COMMENT '登录地点',
    browser VARCHAR(64) DEFAULT '' COMMENT '浏览器',
    os VARCHAR(64) DEFAULT '' COMMENT '操作系统',
    device_type VARCHAR(20) DEFAULT 'pc' COMMENT '设备类型：pc-电脑 mobile-手机',
    status TINYINT DEFAULT 1 COMMENT '登录状态：1-成功 0-失败',
    msg VARCHAR(255) DEFAULT '' COMMENT '提示消息',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    abnormal TINYINT DEFAULT 0 COMMENT '是否异常：0-正常 1-异常',
    token VARCHAR(500) DEFAULT '' COMMENT '登录Token'
);

-- 岗位表
CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(64) NOT NULL COMMENT '岗位名称',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    description VARCHAR(255) DEFAULT '' COMMENT '岗位描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除'
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_name VARCHAR(128) DEFAULT '' COMMENT '模块名称',
    business_type VARCHAR(64) DEFAULT '' COMMENT '业务类型',
    method VARCHAR(255) DEFAULT '' COMMENT '方法名称',
    request_method VARCHAR(16) DEFAULT '' COMMENT '请求方式',
    oper_name VARCHAR(64) DEFAULT '' COMMENT '操作人员',
    oper_url VARCHAR(500) DEFAULT '' COMMENT '请求URL',
    oper_ip VARCHAR(64) DEFAULT '' COMMENT '操作IP',
    oper_location VARCHAR(255) DEFAULT '' COMMENT '操作地点',
    oper_param TEXT COMMENT '请求参数',
    json_result TEXT COMMENT '返回结果',
    status TINYINT DEFAULT 1 COMMENT '操作状态：1-正常 0-失败',
    error_msg VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
    oper_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
);
