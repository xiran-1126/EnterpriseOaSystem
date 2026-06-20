package com.oa.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.oa.common.BusinessException;
import com.oa.dto.CaptchaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CaptchaService {

    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> ipRequestTimes = new ConcurrentHashMap<>();

    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long ONE_MINUTE_MS = 60 * 1000;

    public CaptchaResponse generateCaptcha(String ip) {
        checkRateLimit(ip);

        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(100, 40, 4, 2);
        String captchaKey = "captcha_" + IdUtil.fastSimpleUUID();
        String captchaText = captcha.getCode();

        captchaStore.put(captchaKey, captchaText.toLowerCase());

        log.debug("生成验证码：key={}, code={}", captchaKey, captchaText);

        return new CaptchaResponse(captchaKey, captcha.getImageBase64Data());
    }

    private void checkRateLimit(String ip) {
        long now = System.currentTimeMillis();
        List<Long> times = ipRequestTimes.computeIfAbsent(ip, k -> new ArrayList<>());

        times.removeIf(time -> now - time > ONE_MINUTE_MS);

        if (times.size() >= MAX_REQUESTS_PER_MINUTE) {
            throw new BusinessException(429, "验证码获取过于频繁，请稍后再试");
        }

        times.add(now);
    }

    public boolean validateCaptcha(String captchaKey, String captcha) {
        if (captchaKey == null || captcha == null) {
            return false;
        }

        String storedCaptcha = captchaStore.get(captchaKey);
        captchaStore.remove(captchaKey);

        if (storedCaptcha == null) {
            return false;
        }

        return storedCaptcha.equalsIgnoreCase(captcha.trim());
    }
}
