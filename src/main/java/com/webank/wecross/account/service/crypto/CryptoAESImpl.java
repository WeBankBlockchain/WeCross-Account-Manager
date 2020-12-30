package com.webank.wecross.account.service.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;

public class CryptoAESImpl implements CryptoInterface {

    private byte[] aesKey;

    @Override
    public void setKey(Object key) {
        this.aesKey = DigestUtils.sha256((String) key);
    }

    @Override
    public byte[] encode(byte[] data) throws Exception {
        byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, IV);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        return cipher.doFinal(data);
    }

    @Override
    public byte[] decode(byte[] data) throws Exception {
        byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(16 * 8, IV);

        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        return cipher.doFinal(data);
    }
}
