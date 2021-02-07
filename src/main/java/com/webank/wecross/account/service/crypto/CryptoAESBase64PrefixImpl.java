package com.webank.wecross.account.service.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoAESBase64PrefixImpl extends CryptoAESImpl {

    public static final String prefixString = "base64://";

    @Override
    public byte[] encode(byte[] data) throws Exception {
        byte[] encode = super.encode(data);
        String base64 = prefixString + Base64.getEncoder().encodeToString(encode);
        return base64.getBytes();
    }

    @Override
    public byte[] decode(byte[] data) throws Exception {
        String stringBase64 = new String(data, StandardCharsets.UTF_8);

        // raw data return ??? not start with prefixString
        if (!stringBase64.startsWith(prefixString)) {
            return data;
        }

        stringBase64 = stringBase64.substring(prefixString.length());
        byte[] base64 = Base64.getDecoder().decode(stringBase64.getBytes(StandardCharsets.UTF_8));
        return super.decode(base64);
    }
}
