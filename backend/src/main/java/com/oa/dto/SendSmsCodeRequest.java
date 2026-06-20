package com.oa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendSmsCodeRequest {

    @NotBlank(message = "验证ID不能为空")
    private String verifyId;
}
