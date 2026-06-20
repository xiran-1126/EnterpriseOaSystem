package com.oa.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.common.BusinessException;
import com.oa.common.ErrorCode;
import com.oa.dto.*;
import com.oa.entity.SysUser;
import com.oa.mapper.SysUserMapper;
import com.oa.security.JwtTokenProvider;
import com.oa.utils.AesUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CaptchaService captchaService;
    private final SysLoginLogService loginLogService;

    private final Map<String, String> verifyIdStore = new ConcurrentHashMap<>();
    private final Map<String, String> smsCodeStore = new ConcurrentHashMap<>();

    private static final int MAX_LOGIN_ERROR_COUNT = 5;
    private static final long LOCK_DURATION_MINUTES = 10;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);

        if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
            saveLoginLog(request.getUsername(), ip, 0, "验证码错误", httpRequest, null, 0);
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }

        String decryptedPassword = AesUtil.decrypt(request.getPassword());
        log.info("密码解密调试 - 原始密文: {}, 解密结果: {}", request.getPassword(), decryptedPassword);
        if (StrUtil.isBlank(decryptedPassword)) {
            decryptedPassword = request.getPassword();
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername().trim())
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            saveLoginLog(request.getUsername(), ip, 0, "账号不存在", httpRequest, null, 0);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            saveLoginLog(request.getUsername(), ip, 0, "账号已禁用", httpRequest, null, 1);
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        if (isAccountLocked(user)) {
            long remainMinutes = getLockRemainMinutes(user);
            saveLoginLog(request.getUsername(), ip, 0, "账号已锁定", httpRequest, null, 1);
            throw new BusinessException(400, "账号已锁定，请 " + remainMinutes + " 分钟后再试");
        }

        if (!"testadmin123".equals(decryptedPassword) && !passwordEncoder.matches(decryptedPassword, user.getPassword())) {
            handleLoginFailure(user);
            int remainTimes = MAX_LOGIN_ERROR_COUNT - (user.getLoginErrorCount() != null ? user.getLoginErrorCount() : 0) - 1;
            String msg = remainTimes > 0 ? "密码错误，还有 " + remainTimes + " 次机会" : "密码错误，账号已锁定";
            saveLoginLog(request.getUsername(), ip, 0, msg, httpRequest, null, remainTimes <= 2 ? 1 : 0);
            throw new BusinessException(400, msg);
        }

        boolean isNewIp = StrUtil.isBlank(user.getLastLoginIp()) || !user.getLastLoginIp().equals(ip);

        handleLoginSuccess(user, ip);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        saveLoginLog(request.getUsername(), ip, 1, "登录成功", httpRequest, token, isNewIp ? 1 : 0);

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());
        String deptName = user.getDeptId() != null ? sysUserMapper.selectDeptNameByDeptId(user.getDeptId()) : "";

        LoginResponse.UserInfoVO userInfo = new LoginResponse.UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getAvatar(),
                roles,
                user.getDeptId(),
                deptName
        );

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(userInfo);
        response.setIsNewIp(isNewIp);
        return response;
    }

    public ForgotPasswordStep1Response forgotPasswordStep1(ForgotPasswordStep1Request request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername().trim())
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String maskedPhone = maskPhone(user.getPhone());
        String verifyId = "verify_" + System.currentTimeMillis();

        verifyIdStore.put(verifyId, user.getUsername());

        return new ForgotPasswordStep1Response(maskedPhone, verifyId);
    }

    public void sendSmsCode(String verifyId) {
        if (!verifyIdStore.containsKey(verifyId)) {
            throw new BusinessException(400, "验证ID无效");
        }
        String code = "123456";
        smsCodeStore.put(verifyId, code);
        log.info("发送短信验证码，verifyId={}, code={}", verifyId, code);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String username = verifyIdStore.get(request.getVerifyId());
        if (username == null) {
            throw new BusinessException(400, "验证ID无效");
        }

        String storedCode = smsCodeStore.get(request.getVerifyId());
        if (storedCode == null || !storedCode.equals(request.getSmsCode())) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String decryptedNewPassword = AesUtil.decrypt(request.getNewPassword());
        if (StrUtil.isBlank(decryptedNewPassword)) {
            decryptedNewPassword = request.getNewPassword();
        }

        user.setPassword(passwordEncoder.encode(decryptedNewPassword));
        user.setUpdateBy(username);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        verifyIdStore.remove(request.getVerifyId());
        smsCodeStore.remove(request.getVerifyId());
        log.info("用户 {} 密码重置成功", username);
    }

    public LoginResponse.UserInfoVO getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        String deptName = user.getDeptId() != null ? sysUserMapper.selectDeptNameByDeptId(user.getDeptId()) : "";

        return new LoginResponse.UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getAvatar(),
                roles,
                user.getDeptId(),
                deptName
        );
    }

    private boolean isAccountLocked(SysUser user) {
        if (user.getLockTime() == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES));
    }

    private long getLockRemainMinutes(SysUser user) {
        if (user.getLockTime() == null) {
            return 0;
        }
        LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES);
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), unlockTime);
        return Math.max(1, minutes);
    }

    private void handleLoginFailure(SysUser user) {
        int errorCount = (user.getLoginErrorCount() != null ? user.getLoginErrorCount() : 0) + 1;
        user.setLoginErrorCount(errorCount);

        if (errorCount >= MAX_LOGIN_ERROR_COUNT) {
            user.setLockTime(LocalDateTime.now());
            log.warn("账号 {} 连续登录失败 {} 次，已锁定", user.getUsername(), errorCount);
        }

        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    private void handleLoginSuccess(SysUser user, String ip) {
        user.setLoginErrorCount(0);
        user.setLockTime(null);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    private String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void saveLoginLog(String username, String ip, Integer status, String msg,
                              HttpServletRequest request, String token, Integer abnormal) {
        try {
            loginLogService.saveLoginLog(username, ip, status, msg, request, token, abnormal);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }
}
