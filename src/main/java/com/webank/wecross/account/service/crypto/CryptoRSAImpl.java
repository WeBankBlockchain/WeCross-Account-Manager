package com.webank.wecross.account.service.crypto;

import com.webank.wecross.account.service.authcode.RSAKeyPairManager;
import javax.crypto.Cipher;

public class CryptoRSAImpl implements CryptoInterface {

    private RSAKeyPairManager rsaKeyPairManager;

    public RSAKeyPairManager getRsaKeyPairManager() {
        return rsaKeyPairManager;
    }

    public void setRsaKeyPairManager(RSAKeyPairManager rsaKeyPairManager) {
        this.rsaKeyPairManager = rsaKeyPairManager;
    }

    @Override
    public void setKey(Object key) {
        setRsaKeyPairManager((RSAKeyPairManager) key);
    }

    @Override
    public byte[] encode(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, rsaKeyPairManager.getKeyPair().getPublic());
        return cipher.doFinal(data);
    }

    @Override
    public byte[] decode(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPairManager.getKeyPair().getPrivate());
        return cipher.doFinal(data);
    }
}
