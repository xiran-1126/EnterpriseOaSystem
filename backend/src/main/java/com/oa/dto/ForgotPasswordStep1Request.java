package com.oa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordStep1Request {

    @NotBlank(message = "账号不能为空")
    private String username;
}
