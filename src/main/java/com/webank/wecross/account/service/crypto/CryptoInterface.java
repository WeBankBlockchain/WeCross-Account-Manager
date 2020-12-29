package com.webank.wecross.account.service.crypto;

/** Database field encryption interface */
public interface CryptoInterface {
    void setKey(Object key);

    byte[] encode(byte[] data) throws Exception;

    byte[] decode(byte[] data) throws Exception;
}
