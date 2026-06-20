package com.oa.controller;

import com.oa.common.Result;
import com.oa.dto.*;
import com.oa.entity.SysDept;
import com.oa.entity.SysPost;
import com.oa.entity.SysRole;
import com.oa.security.LoginUser;
import com.oa.service.SysUserService;
import com.oa.vo.PageResult;
import com.oa.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> getUserPage(UserQueryDTO query) {
        return Result.success(sysUserService.getUserPage(query));
    }

    @Operation(summary = "根据ID获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(sysUserService.getUserById(id));
    }

    @Operation(summary = "校验工号是否唯一")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsernameUnique(@RequestParam String username) {
        return Result.success(sysUserService.checkUsernameUnique(username));
    }

    @Operation(summary = "校验手机号是否唯一")
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhoneUnique(@RequestParam String phone, @RequestParam(required = false) Long userId) {
        return Result.success(sysUserService.checkPhoneUnique(phone, userId));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody CreateUserDTO dto,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.createUser(dto, loginUser.getUsername());
        return Result.success("用户创建成功", null);
    }

    @Operation(summary = "编辑用户")
    @PutMapping
    public Result<Void> updateUser(@Valid @RequestBody UpdateUserDTO dto,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.updateUser(dto, loginUser.getUsername());
        return Result.success("用户更新成功", null);
    }

    @Operation(summary = "启用用户")
    @PutMapping("/enable/{id}")
    public Result<Void> enableUser(@PathVariable Long id,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.updateUserStatus(id, 1, loginUser.getUsername());
        return Result.success("用户启用成功", null);
    }

    @Operation(summary = "禁用用户")
    @PutMapping("/disable/{id}")
    public Result<Void> disableUser(@PathVariable Long id,
                                    @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.updateUserStatus(id, 0, loginUser.getUsername());
        return Result.success("用户禁用成功", null);
    }

    @Operation(summary = "批量启用用户")
    @PutMapping("/batch-enable")
    public Result<Void> batchEnableUser(@RequestBody List<Long> userIds,
                                        @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.batchUpdateStatus(userIds, 1, loginUser.getUsername());
        return Result.success("批量启用成功", null);
    }

    @Operation(summary = "批量禁用用户")
    @PutMapping("/batch-disable")
    public Result<Void> batchDisableUser(@RequestBody List<Long> userIds,
                                         @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.batchUpdateStatus(userIds, 0, loginUser.getUsername());
        return Result.success("批量禁用成功", null);
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/reset-password")
    public Result<String> resetUserPassword(@Valid @RequestBody ResetUserPasswordDTO dto,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        String newPassword = sysUserService.resetUserPassword(dto, loginUser.getUsername());
        return Result.success("密码重置成功", newPassword);
    }

    @Operation(summary = "删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.deleteUser(id, loginUser.getUsername());
        return Result.success("用户删除成功", null);
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> userIds,
                                         @AuthenticationPrincipal LoginUser loginUser) {
        sysUserService.batchDeleteUsers(userIds, loginUser.getUsername());
        return Result.success("批量删除成功", null);
    }

    @Operation(summary = "导出用户列表")
    @GetMapping("/export")
    public Result<List<UserVO>> exportUsers(UserQueryDTO query) {
        return Result.success(sysUserService.exportUsers(query));
    }

    @Operation(summary = "获取部门树列表")
    @GetMapping("/dept/tree")
    public Result<List<SysDept>> getDeptTree() {
        return Result.success(sysUserService.getDeptTree());
    }

    @Operation(summary = "根据部门ID获取岗位列表")
    @GetMapping("/post/list")
    public Result<List<SysPost>> getPostsByDeptId(@RequestParam Long deptId) {
        return Result.success(sysUserService.getPostsByDeptId(deptId));
    }

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/role/list")
    public Result<List<SysRole>> getAllRoles() {
        return Result.success(sysUserService.getAllRoles());
    }
}
