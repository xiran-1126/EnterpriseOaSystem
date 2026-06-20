package com.oa.common;

import com.oa.security.LoginUser;

public class PermissionUtils {

    public static void checkAdminPermission(LoginUser loginUser) {
        if (loginUser == null || !loginUser.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }
    }

    public static void checkViewPermission(LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException(401, "未登录");
        }
        if (!loginUser.isAdmin() && !loginUser.isManager()) {
            throw new BusinessException(403, "无权限查看用户管理");
        }
    }
}
