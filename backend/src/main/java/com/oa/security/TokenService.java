package com.oa.security;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKENS_PREFIX = "user:tokens:";

    private final ConcurrentHashMap<String, Long> localBlacklist = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<String>> localUserTokens = new ConcurrentHashMap<>();

    private boolean redisAvailable = true;

    public void saveToken(Long userId, String token) {
        try {
            if (redisAvailable) {
                redisTemplate.opsForSet().add(USER_TOKENS_PREFIX + userId, token);
                redisTemplate.expire(USER_TOKENS_PREFIX + userId, expiration, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis不可用，使用本地缓存存储用户Token");
        }
        localUserTokens.computeIfAbsent(userId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(token);
    }

    public boolean validateToken(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        try {
            if (redisAvailable) {
                return Boolean.FALSE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
            }
        } catch (Exception e) {
            redisAvailable = false;
        }
        return !localBlacklist.containsKey(token);
    }

    public void invalidateToken(String token) {
        try {
            if (redisAvailable) {
                redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", expiration, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            redisAvailable = false;
        }
        localBlacklist.put(token, System.currentTimeMillis() + expiration);
    }

    public void invalidateUserTokens(Long userId) {
        try {
            if (redisAvailable) {
                Set<String> tokens = redisTemplate.opsForSet().members(USER_TOKENS_PREFIX + userId);
                if (tokens != null && !tokens.isEmpty()) {
                    for (String token : tokens) {
                        redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", expiration, TimeUnit.MILLISECONDS);
                    }
                }
                redisTemplate.delete(USER_TOKENS_PREFIX + userId);
            }
        } catch (Exception e) {
            redisAvailable = false;
        }
        Set<String> tokens = localUserTokens.remove(userId);
        if (tokens != null) {
            long expireTime = System.currentTimeMillis() + expiration;
            for (String token : tokens) {
                localBlacklist.put(token, expireTime);
            }
        }
    }

    public void refreshTokenExpire(Long userId, String token) {
        try {
            if (redisAvailable) {
                redisTemplate.expire(USER_TOKENS_PREFIX + userId, expiration, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            redisAvailable = false;
        }
    }
}
