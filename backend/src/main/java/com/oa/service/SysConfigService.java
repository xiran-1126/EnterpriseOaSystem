package com.oa.service;

import cn.hutool.core.util.StrUtil;
import com.oa.common.BusinessException;
import com.oa.common.ErrorCode;
import com.oa.utils.RsaUtil;
import com.oa.vo.PublicKeyVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private static final String RSA_PUBLIC_KEY_KEY = "sys:config:rsa:public";
    private static final String RSA_PRIVATE_KEY_KEY = "sys:config:rsa:private";
    private static final long RSA_KEY_EXPIRE_DAYS = 7;

    private final StringRedisTemplate redisTemplate;

    private boolean redisAvailable = true;

    private final Map<String, String> localCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            String publicKey = getFromRedis(RSA_PUBLIC_KEY_KEY);
            if (StrUtil.isBlank(publicKey)) {
                generateAndSaveKeyPair();
            }
            log.info("RSA密钥对初始化成功");
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis不可用，使用本地缓存存储RSA密钥对");
            if (StrUtil.isBlank(localCache.get(RSA_PUBLIC_KEY_KEY))) {
                generateAndSaveKeyPair();
            }
        }
    }

    private void generateAndSaveKeyPair() {
        KeyPair keyPair = RsaUtil.generateKeyPair();
        String publicKey = RsaUtil.getPublicKeyBase64(keyPair);
        String privateKey = RsaUtil.getPrivateKeyBase64(keyPair);
        saveToCache(RSA_PUBLIC_KEY_KEY, publicKey);
        saveToCache(RSA_PRIVATE_KEY_KEY, privateKey);
    }

    private String getFromRedis(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            redisAvailable = false;
            return localCache.get(key);
        }
    }

    private void saveToRedis(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            redisAvailable = false;
            localCache.put(key, value);
        }
    }

    private void saveToCache(String key, String value) {
        if (redisAvailable) {
            saveToRedis(key, value, RSA_KEY_EXPIRE_DAYS, TimeUnit.DAYS);
        } else {
            localCache.put(key, value);
        }
    }

    private String getFromCache(String key) {
        if (redisAvailable) {
            String value = getFromRedis(key);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return localCache.get(key);
    }

    public PublicKeyVO getPublicKey() {
        String publicKey = getFromCache(RSA_PUBLIC_KEY_KEY);
        if (StrUtil.isBlank(publicKey)) {
            generateAndSaveKeyPair();
            publicKey = getFromCache(RSA_PUBLIC_KEY_KEY);
        }
        return new PublicKeyVO(publicKey);
    }

    public String decryptByPrivateKey(String ciphertext) {
        if (StrUtil.isBlank(ciphertext)) {
            return null;
        }
        String privateKey = getFromCache(RSA_PRIVATE_KEY_KEY);
        if (StrUtil.isBlank(privateKey)) {
            throw new BusinessException(500, "RSA私钥不存在，请刷新页面重试");
        }
        try {
            return RsaUtil.decrypt(ciphertext, privateKey);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new BusinessException(500, "密码解密失败，请刷新页面重试");
        }
    }

    public void refreshKeyPair() {
        generateAndSaveKeyPair();
        log.info("RSA密钥对已刷新");
    }
}
