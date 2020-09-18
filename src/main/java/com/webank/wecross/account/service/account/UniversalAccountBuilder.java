package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.db.ChainAccountTableBean;
import com.webank.wecross.account.service.db.UniversalAccountTableBean;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.NewUAException;
import com.webank.wecross.account.service.utils.SM2;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;
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
                        .secKey(tableBean.getSec())
                        .build();
        ua.setChainAccounts(chainAccounts);
        return ua;
    }

    public static UniversalAccount newUA(String username, String password)
            throws AccountManagerException {
        try {
            List<ChainAccount> chainAccounts = new LinkedList<>();

            KeyPair keyPair = SM2.newKeyPair();
            String sec = SM2.toPemContent(keyPair);
            String pub = SM2.toPubHexString(keyPair);

            UniversalAccount ua =
                    UniversalAccount.builder()
                            .username(username)
                            .pubKey(pub)
                            .uaID(pub)
                            .role("User")
                            .password(password)
                            .secKey(sec)
                            .build();
            ua.setChainAccounts(chainAccounts);
            logger.debug("New UA success: {} {}", username, pub);
            return ua;
        } catch (Exception e) {
            logger.error("New UA failed: " + e);
            throw new NewUAException(e.getMessage());
        }
    }
}
