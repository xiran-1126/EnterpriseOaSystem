package com.oa.controller;

import com.oa.common.Result;
import com.oa.dto.*;
import com.oa.security.LoginUser;
import com.oa.service.AuthService;
import com.oa.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证接口", description = "登录、注册、验证码等认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> getCaptcha(HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return Result.success(captchaService.generateCaptcha(ip));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success("登录成功", authService.login(request, httpRequest));
    }

    @Operation(summary = "忘记密码-第一步：账号验证")
    @PostMapping("/forgot-password/step1")
    public Result<ForgotPasswordStep1Response> forgotPasswordStep1(
            @Valid @RequestBody ForgotPasswordStep1Request request) {
        return Result.success(authService.forgotPasswordStep1(request));
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/send-sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest request) {
        authService.sendSmsCode(request.getVerifyId());
        return Result.success("验证码已发送", null);
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<LoginResponse.UserInfoVO> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(authService.getUserInfo(loginUser.getUserId()));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
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
}
