package com.oa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private UserInfoVO userInfo;

    private Boolean isNewIp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoVO {
        private Long id;
        private String username;
        private String realName;
        private String avatar;
        private List<String> roles;
        private Long deptId;
        private String deptName;
    }
}
