package com.oa.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

import java.security.KeyPair;
import java.util.Base64;

public class RsaUtil {

    public static KeyPair generateKeyPair() {
        return SecureUtil.generateKeyPair("RSA");
    }

    public static String getPublicKeyBase64(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public static String getPrivateKeyBase64(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    public static String encrypt(String plaintext, String publicKeyBase64) {
        RSA rsa = new RSA(null, publicKeyBase64);
        return rsa.encryptBase64(plaintext, KeyType.PublicKey);
    }

    public static String decrypt(String ciphertext, String privateKeyBase64) {
        RSA rsa = new RSA(privateKeyBase64, null);
        return rsa.decryptStr(ciphertext, KeyType.PrivateKey);
    }
}
