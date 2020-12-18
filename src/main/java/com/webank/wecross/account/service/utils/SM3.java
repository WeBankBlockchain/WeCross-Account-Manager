package com.webank.wecross.account.service.utils;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

public class SM3 {
    public static String hash(String content) {
        byte[] contentBytes = content.getBytes();
        byte[] md = new byte[32];
        SM3Digest sm3 = new SM3Digest();
        sm3.update(contentBytes, 0, contentBytes.length);
        sm3.doFinal(md, 0);
        String result = new String(Hex.encode(md));
        return result;
    }
}
