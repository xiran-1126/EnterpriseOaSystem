package com.oa.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.oa.entity.SysLoginLog;
import com.oa.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogService {

    private final SysLoginLogMapper sysLoginLogMapper;

    @Async
    public void saveLoginLog(String username, String ip, Integer status, String msg,
                             HttpServletRequest request, String token, Integer abnormal) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setIpAddr(ip);
        loginLog.setStatus(status);
        loginLog.setMsg(msg);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setToken(token);
        loginLog.setAbnormal(abnormal != null ? abnormal : 0);

        String userAgentStr = request.getHeader("User-Agent");
        if (StrUtil.isNotBlank(userAgentStr)) {
            UserAgent ua = UserAgentUtil.parse(userAgentStr);
            loginLog.setBrowser(ua.getBrowser().getName());
            loginLog.setOs(ua.getOs().getName());
            loginLog.setDeviceType(ua.isMobile() ? "mobile" : "pc");
        } else {
            loginLog.setDeviceType("pc");
        }

        sysLoginLogMapper.insert(loginLog);
    }

    public String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
