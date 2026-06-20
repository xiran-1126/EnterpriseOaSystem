package com.oa.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private Long deptId;

    private List<String> roles;

    public boolean isAdmin() {
        return roles != null && roles.contains("admin");
    }

    public boolean isManager() {
        return roles != null && (roles.contains("admin") || roles.contains("manager") || roles.contains("leader"));
    }
}
