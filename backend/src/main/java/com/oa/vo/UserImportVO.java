package com.oa.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserImportVO {

    @ExcelProperty("工号")
    private String username;

    @ExcelProperty("姓名")
    private String realName;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("部门名称")
    private String deptName;

    @ExcelProperty("岗位名称")
    private String postName;

    @ExcelProperty("角色名称")
    private String roleName;

    @ExcelProperty("备注")
    private String remark;
}
