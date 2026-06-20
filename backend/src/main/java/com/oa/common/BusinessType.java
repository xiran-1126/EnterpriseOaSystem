package com.oa.common;

public enum BusinessType {
    OTHER(0, "其他"),
    INSERT(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    GRANT(4, "授权"),
    EXPORT(5, "导出"),
    IMPORT(6, "导入"),
    FORCE(7, "强退"),
    STATUS(8, "更新状态"),
    RESET_PASSWORD(9, "重置密码");

    private final int code;
    private final String description;

    BusinessType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
