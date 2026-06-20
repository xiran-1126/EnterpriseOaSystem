package com.oa.common;

public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    CAPTCHA_ERROR(401, "验证码错误"),
    USER_NOT_FOUND(402, "账号不存在"),
    USER_DISABLED(403, "账号已禁用"),
    PASSWORD_ERROR(404, "密码错误"),
    ACCOUNT_LOCKED(405, "账号已锁定"),
    SMS_CODE_ERROR(406, "短信验证码错误或已过期"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "没有权限"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
