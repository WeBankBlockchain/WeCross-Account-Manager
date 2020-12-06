package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.account.LoginSalt;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.authcode.AuthCodeManager;
import com.webank.wecross.account.service.authcode.RSAKeyPairManager;
import com.webank.wecross.account.service.db.ChainAccountTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ConfigurationException;
import com.webank.wecross.account.service.utils.FileUtility;
import com.webank.wecross.account.service.utils.RSAUtility;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

@Configuration
public class UAManagerConfig {
    @Autowired UniversalAccountTableJPA universalAccountTableJPA;

    @Autowired ChainAccountTableJPA chainAccountTableJPA;

    @Resource ApplicationConfig applicationConfig;

    @Bean
    public UAManager newUAManager() throws AccountManagerException {
        UAManager uaManager = new UAManager();
        uaManager.setUniversalAccountTableJPA(universalAccountTableJPA);
        uaManager.setChainAccountTableJPA(chainAccountTableJPA);
        String username = applicationConfig.getAdmin().getUsername();
        String password = applicationConfig.getAdmin().getPassword();
        String confusedPassword = DigestUtils.sha256Hex(LoginSalt.LoginSalt + password);
        uaManager.initAdminUA(username, confusedPassword);

        // uaManager.addMockUA();

        return uaManager;
    }

    @Bean
    public AuthCodeManager newAuthCodeManager() {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("AuthCode-"));

        ApplicationConfig.Ext ext = applicationConfig.getExt();
        AuthCodeManager authCodeManager = new AuthCodeManager(scheduledExecutorService);
        authCodeManager.setAllowImageAuthCodeEmpty(ext.isAllowImageAuthCodeEmpty());
        return authCodeManager;
    }

    @Bean
    public RSAKeyPairManager newRSAKeyPairManager()
            throws NoSuchAlgorithmException, ConfigurationException, InvalidKeySpecException,
                    IOException {
        RSAKeyPairManager rsaKeyPairManager = new RSAKeyPairManager();

        String privateKeyContent =
                FileUtility.readFileContent(applicationConfig.getEncrypt().getPrivateKey());
        String publicKeyContent =
                FileUtility.readFileContent(applicationConfig.getEncrypt().getPublicKey());
        PrivateKey privateKey = RSAUtility.createPrivateKey(privateKeyContent);
        PublicKey publicKey = RSAUtility.createPublicKey(publicKeyContent);
        rsaKeyPairManager.setKeyPair(new KeyPair(publicKey, privateKey));
        return rsaKeyPairManager;
    }
}
