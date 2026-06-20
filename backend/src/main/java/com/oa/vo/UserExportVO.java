package com.oa.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(15)
public class UserExportVO {

    @ExcelProperty("工号")
    @ColumnWidth(12)
    private String username;

    @ExcelProperty("姓名")
    @ColumnWidth(10)
    private String realName;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("部门")
    @ColumnWidth(12)
    private String deptName;

    @ExcelProperty("岗位")
    @ColumnWidth(12)
    private String postName;

    @ExcelProperty("角色")
    @ColumnWidth(15)
    private String roleNames;

    @ExcelProperty("账号状态")
    @ColumnWidth(10)
    private String statusText;

    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private String createTime;
}
