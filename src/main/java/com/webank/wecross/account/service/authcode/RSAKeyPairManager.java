package com.webank.wecross.account.service.authcode;

import java.security.KeyPair;

public class RSAKeyPairManager {

    private KeyPair keyPair;

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
}
