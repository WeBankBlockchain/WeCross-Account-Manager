package com.webank.wecross.account.service.crypto;

import java.util.Base64;

public class CryptoAESBase64Impl extends CryptoAESImpl {
    @Override
    public byte[] encode(byte[] data) throws Exception {
        byte[] encode = super.encode(data);
        /** base64 encode */
        return Base64.getEncoder().encode(encode);
    }

    @Override
    public byte[] decode(byte[] data) throws Exception {
        /** base64 decode */
        byte[] base64 = Base64.getDecoder().decode(data);
        return super.decode(base64);
    }
}
