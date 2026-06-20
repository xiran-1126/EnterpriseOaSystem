package com.oa.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.BusinessException;
import com.oa.common.ErrorCode;
import com.oa.dto.CreateUserDTO;
import com.oa.dto.ResetUserPasswordDTO;
import com.oa.dto.UpdateUserDTO;
import com.oa.dto.UserQueryDTO;
import com.oa.entity.SysDept;
import com.oa.entity.SysPost;
import com.oa.entity.SysRole;
import com.oa.entity.SysUser;
import com.oa.entity.SysUserRole;
import com.oa.mapper.SysDeptMapper;
import com.oa.mapper.SysPostMapper;
import com.oa.mapper.SysRoleMapper;
import com.oa.mapper.SysUserMapper;
import com.oa.mapper.SysUserRoleMapper;
import com.oa.vo.PageResult;
import com.oa.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysPostMapper sysPostMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> getUserPage(UserQueryDTO query) {
        Page<UserVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<UserVO> result = sysUserMapper.selectUserPage(page, query);
        
        List<UserVO> list = result.getRecords();
        for (UserVO user : list) {
            if (user.getId() != null) {
                List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(user.getId());
                user.setRoleIds(roleIds);
                if (roleIds != null && !roleIds.isEmpty()) {
                    List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
                    String roleNames = roles.stream()
                            .map(SysRole::getRoleName)
                            .collect(Collectors.joining(", "));
                    user.setRoleNames(roleNames);
                }
            }
            if (StrUtil.isNotBlank(user.getPhone()) && user.getPhone().length() >= 7) {
                user.setPhone(user.getPhone().substring(0, 3) + "****" + user.getPhone().substring(user.getPhone().length() - 4));
            }
        }

        return new PageResult<>(list, result.getTotal(), query.getPageNum(), query.getPageSize());
    }

    public UserVO getUserById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        userVO.setDeptId(user.getDeptId());
        userVO.setPostId(user.getPostId());
        userVO.setStatus(user.getStatus());
        userVO.setRemark(user.getRemark());
        userVO.setCreateTime(user.getCreateTime());

        if (user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            if (dept != null) {
                userVO.setDeptName(dept.getDeptName());
            }
        }

        if (user.getPostId() != null) {
            SysPost post = sysPostMapper.selectById(user.getPostId());
            if (post != null) {
                userVO.setPostName(post.getPostName());
            }
        }

        List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(userId);
        userVO.setRoleIds(roleIds);
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            String roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.joining(", "));
            userVO.setRoleNames(roleNames);
        }

        return userVO;
    }

    public boolean checkUsernameUnique(String username) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );
        return count == 0;
    }

    public boolean checkPhoneUnique(String phone, Long userId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getDeleted, 0);
        if (userId != null) {
            wrapper.ne(SysUser::getId, userId);
        }
        Long count = sysUserMapper.selectCount(wrapper);
        return count == 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserDTO dto, String operator) {
        if (!checkUsernameUnique(dto.getUsername())) {
            throw new BusinessException(400, "工号已存在，请更换工号");
        }

        SysDept dept = sysDeptMapper.selectById(dto.getDeptId());
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException(400, "所属部门不存在");
        }

        SysPost post = sysPostMapper.selectById(dto.getPostId());
        if (post == null || post.getDeleted() == 1 || !post.getDeptId().equals(dto.getDeptId())) {
            throw new BusinessException(400, "岗位不存在或不属于所选部门");
        }

        if (!checkPhoneUnique(dto.getPhone(), null)) {
            throw new BusinessException(400, "该手机号已绑定其他账号");
        }

        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            throw new BusinessException(400, "请至少选择一个角色");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setPostId(dto.getPostId());
        user.setRemark(dto.getRemark());
        user.setStatus(1);
        user.setCreateBy(operator);
        user.setUpdateBy(operator);

        String password = dto.getPassword();
        if (StrUtil.isBlank(password)) {
            password = generateRandomPassword();
        }
        user.setPassword(passwordEncoder.encode(password));

        sysUserMapper.insert(user);

        for (Long roleId : dto.getRoleIds()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        }

        log.info("用户创建成功，userId={}, username={}", user.getId(), user.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UpdateUserDTO dto, String operator) {
        SysUser user = sysUserMapper.selectById(dto.getId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        SysDept dept = sysDeptMapper.selectById(dto.getDeptId());
        if (dept == null || dept.getDeleted() == 1) {
            throw new BusinessException(400, "所属部门不存在");
        }

        SysPost post = sysPostMapper.selectById(dto.getPostId());
        if (post == null || post.getDeleted() == 1 || !post.getDeptId().equals(dto.getDeptId())) {
            throw new BusinessException(400, "岗位不存在或不属于所选部门");
        }

        if (!checkPhoneUnique(dto.getPhone(), dto.getId())) {
            throw new BusinessException(400, "该手机号已绑定其他账号");
        }

        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            throw new BusinessException(400, "请至少选择一个角色");
        }

        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setPostId(dto.getPostId());
        user.setRemark(dto.getRemark());
        user.setUpdateBy(operator);
        user.setUpdateTime(LocalDateTime.now());

        sysUserMapper.updateById(user);

        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, dto.getId())
        );

        for (Long roleId : dto.getRoleIds()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(dto.getId());
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        }

        log.info("用户更新成功，userId={}", dto.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status, String operator) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (status == 0 && "admin".equals(user.getUsername())) {
            throw new BusinessException(400, "系统管理员账号不可禁用");
        }

        user.setStatus(status);
        user.setUpdateBy(operator);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("用户状态更新成功，userId={}, status={}", userId, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> userIds, Integer status, String operator) {
        for (Long userId : userIds) {
            updateUserStatus(userId, status, operator);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String resetUserPassword(ResetUserPasswordDTO dto, String operator) {
        SysUser user = sysUserMapper.selectById(dto.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String newPassword = dto.getNewPassword();
        if (StrUtil.isBlank(newPassword)) {
            newPassword = generateRandomPassword();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateBy(operator);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("用户密码重置成功，userId={}", dto.getUserId());
        return newPassword;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId, String operator) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(400, "系统管理员账号不可删除");
        }

        sysUserMapper.deleteById(userId);

        log.info("用户删除成功，userId={}", userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds, String operator) {
        for (Long userId : userIds) {
            deleteUser(userId, operator);
        }
    }

    public List<UserVO> exportUsers(UserQueryDTO query) {
        List<UserVO> list = sysUserMapper.selectUserList(query);
        for (UserVO user : list) {
            if (user.getId() != null) {
                List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(user.getId());
                user.setRoleIds(roleIds);
                if (roleIds != null && !roleIds.isEmpty()) {
                    List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
                    String roleNames = roles.stream()
                            .map(SysRole::getRoleName)
                            .collect(Collectors.joining(", "));
                    user.setRoleNames(roleNames);
                }
            }
        }
        return list;
    }

    private String generateRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String number = "0123456789";
        String specialChar = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        String allChar = upperCase + lowerCase + number + specialChar;
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 2; i++) {
            password.append(upperCase.charAt(RandomUtil.randomInt(upperCase.length())));
        }
        for (int i = 0; i < 2; i++) {
            password.append(lowerCase.charAt(RandomUtil.randomInt(lowerCase.length())));
        }
        for (int i = 0; i < 2; i++) {
            password.append(number.charAt(RandomUtil.randomInt(number.length())));
        }
        for (int i = 0; i < 2; i++) {
            password.append(specialChar.charAt(RandomUtil.randomInt(specialChar.length())));
        }

        for (int i = 0; i < 4; i++) {
            password.append(allChar.charAt(RandomUtil.randomInt(allChar.length())));
        }

        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RandomUtil.randomInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    public List<SysDept> getDeptTree() {
        return sysDeptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getDeleted, 0)
                        .eq(SysDept::getStatus, 1)
                        .orderByAsc(SysDept::getOrderNum)
        );
    }

    public List<SysPost> getPostsByDeptId(Long deptId) {
        return sysPostMapper.selectPostsByDeptId(deptId);
    }

    public List<SysRole> getAllRoles() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDeleted, 0)
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getId)
        );
    }
}
