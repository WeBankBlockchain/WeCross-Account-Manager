package com.webank.wecross.account.service.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class CommonUtility {
    /**
     * Use the salt mix the password
     *
     * @param pwd
     * @param salt
     * @return
     */
    public static String generateMixedPwdWithSalt(String pwd, String salt) {
        // Hash(salt + Hash(pwd))
        return DigestUtils.sha256Hex(salt + DigestUtils.sha256Hex(pwd));
    }
}