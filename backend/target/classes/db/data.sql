MERGE INTO sys_dept (id, parent_id, dept_name, order_num, leader, phone, email, status) KEY(id)
VALUES (1, 0, '总公司', 0, '张总', '13800000001', 'ceo@company.com', 1),
       (2, 1, '技术部', 1, '李总监', '13800000002', 'tech@company.com', 1),
       (3, 1, '市场部', 2, '王总监', '13800000003', 'market@company.com', 1),
       (4, 1, '人事部', 3, '赵总监', '13800000004', 'hr@company.com', 1),
       (5, 1, '财务部', 4, '钱总监', '13800000005', 'finance@company.com', 1),
       (6, 1, '行政部', 5, '孙总监', '13800000006', 'admin@company.com', 1);

MERGE INTO sys_role (id, role_name, role_code, description, status) KEY(id)
VALUES (1, '超级管理员', 'admin', '拥有所有权限', 1),
       (2, '普通员工', 'employee', '普通员工权限', 1),
       (3, '部门主管', 'manager', '部门主管权限', 1),
       (4, '财务人员', 'finance', '财务人员权限', 1),
       (5, 'HR管理员', 'hr', '人力资源管理员', 1),
       (6, '行政人员', 'admin_staff', '行政人员权限', 1),
       (7, '领导层', 'leader', '领导层权限', 1);

MERGE INTO sys_post (id, post_code, post_name, dept_id, order_num, description, status) KEY(id)
VALUES (1, 'TECH_DIRECTOR', '技术总监', 2, 1, '技术部门总负责人', 1),
       (2, 'SENIOR_DEV', '高级开发工程师', 2, 2, '高级软件开发工程师', 1),
       (3, 'JUNIOR_DEV', '初级开发工程师', 2, 3, '初级软件开发工程师', 1),
       (4, 'MARKET_DIRECTOR', '市场总监', 3, 1, '市场部门总负责人', 1),
       (5, 'MARKET_SPECIALIST', '市场专员', 3, 2, '市场推广专员', 1),
       (6, 'HR_MANAGER', '人事经理', 4, 1, '人力资源部门经理', 1),
       (7, 'HR_SPECIALIST', '人事专员', 4, 2, '人力资源专员', 1),
       (8, 'FINANCE_MANAGER', '财务经理', 5, 1, '财务部门经理', 1),
       (9, 'ACCOUNTANT', '会计', 5, 2, '财务会计', 1),
       (10, 'ADMIN_MANAGER', '行政经理', 6, 1, '行政部门经理', 1),
       (11, 'ADMIN_SPECIALIST', '行政专员', 6, 2, '行政专员', 1);

MERGE INTO sys_user (id, username, password, real_name, avatar, phone, email, dept_id, post_id, remark, status) KEY(id)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '', '13800138000', 'admin@company.com', 2, 1, '系统超级管理员账号', 1),
       (2, 'zhangsan', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张三', '', '13900139000', 'zhangsan@company.com', 3, 5, '市场部员工', 1);

MERGE INTO sys_user_role (user_id, role_id) KEY(user_id, role_id)
VALUES (1, 1),
       (2, 2);
