package com.oa.service;

import com.oa.entity.SysOperLog;
import com.oa.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysOperLogService {

    private final SysOperLogMapper sysOperLogMapper;

    @Async
    public void saveOperLog(SysOperLog operLog) {
        sysOperLogMapper.insert(operLog);
    }
}
