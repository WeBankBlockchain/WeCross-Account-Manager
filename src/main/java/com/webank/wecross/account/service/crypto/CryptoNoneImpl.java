package com.webank.wecross.account.service.crypto;

public class CryptoNoneImpl implements CryptoInterface {
    @Override
    public void setKey(Object key) {
        // do nothing
    }

    @Override
    public byte[] encode(byte[] data) {
        return data;
    }

    @Override
    public byte[] decode(byte[] data) {
        return data;
    }
}
