package com.oa.controller;

import com.oa.common.Result;
import com.oa.service.SysConfigService;
import com.oa.vo.PublicKeyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统配置", description = "系统配置相关接口")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "获取RSA公钥")
    @GetMapping("/public-key")
    public Result<PublicKeyVO> getPublicKey() {
        return Result.success(sysConfigService.getPublicKey());
    }
}
