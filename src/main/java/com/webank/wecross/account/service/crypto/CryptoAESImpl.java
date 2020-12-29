package com.webank.wecross.account.service.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;

public class CryptoAESImpl implements CryptoInterface {

    private byte[] aesKey;

    public void updateAESKey(String key) {
        this.aesKey = genAESSecretKey(key);
    }

    public byte[] genAESSecretKey(String key) {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getCause());
        }
        keyGenerator.init(256, new SecureRandom(DigestUtils.sha256(key)));

        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    @Override
    public void setKey(Object key) {
        updateAESKey((String) key);
    }

    @Override
    public byte[] encode(byte[] data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    @Override
    public byte[] decode(byte[] data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }
}
