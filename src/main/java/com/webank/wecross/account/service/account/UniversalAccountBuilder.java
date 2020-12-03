package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.db.ChainAccountTableBean;
import com.webank.wecross.account.service.db.UniversalAccountTableBean;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ErrorCode;
import com.webank.wecross.account.service.utils.PassWordUtility;
import com.webank.wecross.account.service.utils.SM2;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalAccountBuilder {
    private static Logger logger = LoggerFactory.getLogger(UniversalAccountBuilder.class);

    public static UniversalAccount build(
            UniversalAccountTableBean tableBean, List<ChainAccountTableBean> chainAccountTableBeans)
            throws AccountManagerException {
        List<ChainAccount> chainAccounts = new LinkedList<>();
        for (ChainAccountTableBean chainAccountTableBean : chainAccountTableBeans) {
            ChainAccount chainAccount =
                    ChainAccountBuilder.buildFromTableBean(chainAccountTableBean);
            chainAccounts.add(chainAccount);
        }

        UniversalAccount ua =
                UniversalAccount.builder()
                        .id(tableBean.getId())
                        .username(tableBean.getUsername())
                        .pubKey(tableBean.getPub())
                        .uaID(tableBean.getUaID())
                        .role(tableBean.getRole())
                        .password(tableBean.getPassword())
                        .salt(tableBean.getSalt())
                        .tokenSec(tableBean.getTokenSec())
                        .secKey(tableBean.getSec())
                        .latestKeyID(tableBean.getLatestKeyID())
                        .version(tableBean.getVersion())
                        .build();
        ua.setChainAccounts(chainAccounts);
        return ua;
    }

    /**
     * newUA with salt of the password, automatic generate the salt
     *
     * @param username
     * @param password
     * @return
     * @throws AccountManagerException
     */
    public static UniversalAccount newUA(String username, String password)
            throws AccountManagerException {
        String salt = UUID.randomUUID().toString();
        return newUA(username, password, salt);
    }

    /**
     * @param username
     * @param password
     * @param salt
     * @return
     * @throws AccountManagerException
     */
    private static UniversalAccount newUA(String username, String password, String salt)
            throws AccountManagerException {
        try {
            List<ChainAccount> chainAccounts = new LinkedList<>();

            KeyPair keyPair = SM2.newKeyPair();
            String sec = SM2.toPemContent(keyPair);
            String pub = SM2.toPubHexString(keyPair);
            String tokenSec = newTokenStr();

            UniversalAccount ua =
                    UniversalAccount.builder()
                            .username(username)
                            .pubKey(pub)
                            .uaID(pub)
                            .role("User")
                            .password(PassWordUtility.mixPassWithSalt(password, salt))
                            .salt(salt)
                            .tokenSec(tokenSec)
                            .secKey(sec)
                            .latestKeyID(0)
                            .version(new Long(0))
                            .build();
            ua.setChainAccounts(chainAccounts);
            logger.debug(
                    "New UA success, username: {}, pub: {}, salt: {}, password: {}",
                    username,
                    pub,
                    salt,
                    ua.getPassword());
            return ua;
        } catch (Exception e) {
            logger.error("New UA failed: " + e);
            throw new AccountManagerException(
                    ErrorCode.NewUAException.getErrorCode(), e.getMessage());
        }
    }

    public static String newTokenStr() {
        // 32 string
        return String.valueOf((new SecureRandom()).nextLong() % 100000000)
                + String.valueOf((new SecureRandom()).nextLong() % 100000000)
                + String.valueOf((new SecureRandom()).nextLong() % 100000000)
                + String.valueOf((new SecureRandom()).nextLong() % 100000000);
    }
}
