package com.oa.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;

import java.nio.charset.StandardCharsets;

public class AesUtil {

    private static final String SECRET_KEY = "oa-system-secret-key-2024";

    private static AES getAes() {
        byte[] keyBytes = new byte[16];
        byte[] temp = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(temp.length, keyBytes.length);
        System.arraycopy(temp, 0, keyBytes, 0, len);
        return SecureUtil.aes(keyBytes);
    }

    public static String decrypt(String ciphertext) {
        try {
            return getAes().decryptStr(ciphertext);
        } catch (Exception e) {
            return null;
        }
    }

    public static String encrypt(String plaintext) {
        try {
            return getAes().encryptBase64(plaintext);
        } catch (Exception e) {
            return null;
        }
    }
}
