package com.oa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserDTO {

    @NotBlank(message = "工号不能为空")
    @Size(min = 2, max = 20, message = "工号长度为2-20位")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "工号只能包含数字和字母")
    private String username;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 10, message = "姓名长度不能超过10个字符")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    @NotNull(message = "岗位不能为空")
    private Long postId;

    @NotNull(message = "绑定角色不能为空")
    private List<Long> roleIds;

    private String password;

    @Size(max = 200, message = "备注长度不能超过200字")
    private String remark;
}
