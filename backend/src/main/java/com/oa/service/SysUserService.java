package com.oa.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.BusinessException;
import com.oa.common.ErrorCode;
import com.oa.dto.CreateUserDTO;
import com.oa.dto.ResetUserPasswordDTO;
import com.oa.dto.UpdateUserDTO;
import com.oa.dto.UserQueryDTO;
import com.oa.security.LoginUser;
import com.oa.security.TokenService;
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
import com.oa.vo.ImportError;
import com.oa.vo.ImportResultVO;
import com.oa.vo.PageResult;
import com.oa.vo.UserExportVO;
import com.oa.vo.UserImportVO;
import com.oa.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final TokenService tokenService;
    private final SysConfigService sysConfigService;

    public PageResult<UserVO> getUserPage(UserQueryDTO query, LoginUser loginUser) {
        if (!loginUser.isAdmin()) {
            if (loginUser.isManager()) {
                Long deptId = loginUser.getDeptId();
                if (deptId != null) {
                    List<Long> deptIds = getChildDeptIds(deptId);
                    query.setDeptIds(deptIds);
                }
            } else {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

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

        if (status == 0) {
            tokenService.invalidateUserTokens(userId);
            log.info("用户已禁用，强制下线，userId={}", userId);
        }

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
        if (StrUtil.isNotBlank(newPassword)) {
            newPassword = sysConfigService.decryptByPrivateKey(newPassword);
        } else {
            newPassword = generateRandomPassword();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateBy(operator);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        tokenService.invalidateUserTokens(dto.getUserId());
        log.info("用户密码重置成功，强制下线，userId={}", dto.getUserId());

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

        tokenService.invalidateUserTokens(userId);
        log.info("用户删除成功，强制下线，userId={}", userId);

        log.info("用户删除成功，userId={}", userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds, String operator) {
        for (Long userId : userIds) {
            deleteUser(userId, operator);
        }
    }

    public byte[] exportUsersExcel(UserQueryDTO query) {
        List<UserVO> list = sysUserMapper.selectUserList(query);
        List<UserExportVO> exportList = new ArrayList<>();
        for (UserVO user : list) {
            UserExportVO exportVO = new UserExportVO();
            exportVO.setUsername(user.getUsername());
            exportVO.setRealName(user.getRealName());
            exportVO.setPhone(user.getPhone());
            exportVO.setEmail(user.getEmail());
            exportVO.setDeptName(user.getDeptName());
            exportVO.setPostName(user.getPostName());
            exportVO.setStatusText(user.getStatus() == 1 ? "启用" : "禁用");
            exportVO.setRemark(user.getRemark());
            exportVO.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : "");

            if (user.getId() != null) {
                List<Long> roleIds = sysUserMapper.selectRoleIdsByUserId(user.getId());
                if (roleIds != null && !roleIds.isEmpty()) {
                    List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
                    String roleNames = roles.stream()
                            .map(SysRole::getRoleName)
                            .collect(Collectors.joining(", "));
                    exportVO.setRoleNames(roleNames);
                }
            }
            exportList.add(exportVO);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, UserExportVO.class)
                .sheet("用户花名册")
                .doWrite(exportList);
        return outputStream.toByteArray();
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

    private List<Long> getChildDeptIds(Long deptId) {
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(deptId);
        getChildDeptIdsRecursive(deptId, deptIds);
        return deptIds;
    }

    private void getChildDeptIdsRecursive(Long parentId, List<Long> deptIds) {
        List<Long> childIds = sysDeptMapper.selectDeptIdsByParentId(parentId);
        if (childIds != null && !childIds.isEmpty()) {
            deptIds.addAll(childIds);
            for (Long childId : childIds) {
                getChildDeptIdsRecursive(childId, deptIds);
            }
        }
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

    public byte[] downloadTemplate() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ExcelWriter excelWriter = EasyExcel.write(out, UserImportVO.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("用户导入模板").build();
            excelWriter.write(new ArrayList<>(), writeSheet);
            excelWriter.finish();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("生成Excel模板失败", e);
            throw new BusinessException(500, "生成模板失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importUsers(MultipartFile file, String operator) {
        ImportResultVO result = new ImportResultVO();
        List<ImportError> errors = new ArrayList<>();

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "只支持 .xlsx 或 .xls 格式的文件");
        }

        List<UserImportVO> importList;
        try {
            importList = EasyExcel.read(file.getInputStream())
                    .head(UserImportVO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }

        if (importList == null || importList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        if (importList.size() > 1000) {
            throw new BusinessException(400, "单次导入最多支持1000条数据");
        }

        result.setTotalCount(importList.size());

        List<SysDept> depts = sysDeptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeleted, 0).eq(SysDept::getStatus, 1)
        );
        Map<String, SysDept> deptMap = depts.stream()
                .collect(Collectors.toMap(SysDept::getDeptName, d -> d, (d1, d2) -> d1));

        List<SysPost> posts = sysPostMapper.selectList(
                new LambdaQueryWrapper<SysPost>().eq(SysPost::getDeleted, 0).eq(SysPost::getStatus, 1)
        );
        Map<String, SysPost> postMap = posts.stream()
                .collect(Collectors.toMap(SysPost::getPostName, p -> p, (p1, p2) -> p1));

        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, 0).eq(SysRole::getStatus, 1)
        );
        Map<String, SysRole> roleMap = roles.stream()
                .collect(Collectors.toMap(SysRole::getRoleName, r -> r, (r1, r2) -> r1));

        List<String> existingUsernames = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0)
        ).stream().map(SysUser::getUsername).collect(Collectors.toList());

        Set<String> usernameSet = new HashSet<>();
        List<UserImportVO> validList = new ArrayList<>();
        List<SysUser> userList = new ArrayList<>();
        List<SysUserRole> userRoleList = new ArrayList<>();

        String phoneRegex = "^1[3-9]\\d{9}$";

        for (int i = 0; i < importList.size(); i++) {
            int rowNum = i + 2;
            UserImportVO vo = importList.get(i);
            List<String> errorMsgs = new ArrayList<>();

            if (StrUtil.isBlank(vo.getUsername())) {
                errorMsgs.add("工号不能为空");
            } else if (existingUsernames.contains(vo.getUsername()) || usernameSet.contains(vo.getUsername())) {
                errorMsgs.add("工号已存在");
            } else {
                usernameSet.add(vo.getUsername());
            }

            if (StrUtil.isBlank(vo.getRealName())) {
                errorMsgs.add("姓名不能为空");
            }

            if (StrUtil.isBlank(vo.getPhone())) {
                errorMsgs.add("手机号不能为空");
            } else if (!ReUtil.isMatch(phoneRegex, vo.getPhone())) {
                errorMsgs.add("手机号格式不正确");
            }

            SysDept dept = null;
            if (StrUtil.isBlank(vo.getDeptName())) {
                errorMsgs.add("部门名称不能为空");
            } else {
                dept = deptMap.get(vo.getDeptName());
                if (dept == null) {
                    errorMsgs.add("部门名称不存在");
                }
            }

            SysPost post = null;
            if (StrUtil.isBlank(vo.getPostName())) {
                errorMsgs.add("岗位名称不能为空");
            } else {
                post = postMap.get(vo.getPostName());
                if (post == null) {
                    errorMsgs.add("岗位名称不存在");
                } else if (dept != null && !post.getDeptId().equals(dept.getId())) {
                    errorMsgs.add("岗位不属于该部门");
                }
            }

            SysRole role = null;
            if (StrUtil.isBlank(vo.getRoleName())) {
                errorMsgs.add("角色名称不能为空");
            } else {
                role = roleMap.get(vo.getRoleName());
                if (role == null) {
                    errorMsgs.add("角色名称不存在");
                }
            }

            if (!errorMsgs.isEmpty()) {
                errors.add(new ImportError(rowNum, String.join("；", errorMsgs), vo));
            } else {
                validList.add(vo);

                SysUser user = new SysUser();
                user.setUsername(vo.getUsername());
                user.setRealName(vo.getRealName());
                user.setPhone(vo.getPhone());
                user.setEmail(vo.getEmail());
                user.setDeptId(dept.getId());
                user.setPostId(post.getId());
                user.setRemark(vo.getRemark());
                user.setStatus(1);
                user.setCreateBy(operator);
                user.setUpdateBy(operator);
                user.setPassword(passwordEncoder.encode(generateRandomPassword()));
                userList.add(user);

                SysUserRole userRole = new SysUserRole();
                userRole.setRoleId(role.getId());
                userRole.setCreateTime(LocalDateTime.now());
                userRoleList.add(userRole);
            }
        }

        if (!errors.isEmpty()) {
            result.setSuccessCount(0);
            result.setFailCount(errors.size());
            result.setErrors(errors);
            return result;
        }

        for (SysUser user : userList) {
            sysUserMapper.insert(user);
        }

        for (int i = 0; i < userRoleList.size(); i++) {
            userRoleList.get(i).setUserId(userList.get(i).getId());
            sysUserRoleMapper.insert(userRoleList.get(i));
        }

        result.setSuccessCount(validList.size());
        result.setFailCount(0);
        log.info("批量导入用户成功，共{}条", validList.size());

        return result;
    }
}
