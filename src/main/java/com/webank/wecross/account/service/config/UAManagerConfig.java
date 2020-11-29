package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.authcode.AuthCodeManager;
import com.webank.wecross.account.service.authcode.RSAKeyPairManager;
import com.webank.wecross.account.service.db.ChainAccountTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.utils.RSAUtility;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.annotation.Resource;
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
        uaManager.initAdminUA(applicationConfig.admin.username, applicationConfig.admin.password);

        // uaManager.addMockUA();

        return uaManager;
    }

    @Bean
    public AuthCodeManager newAuthCodeManager() {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("AuthCode-"));
        AuthCodeManager authCodeManager = new AuthCodeManager(scheduledExecutorService);
        return authCodeManager;
    }

    @Bean
    public RSAKeyPairManager newRSAKeyPairManager() throws NoSuchAlgorithmException {
        RSAKeyPairManager rsaKeyPairManager = new RSAKeyPairManager();
        // TODO, load rsa private key
        KeyPair keyPair = RSAUtility.buildKeyPair();
        rsaKeyPairManager.setKeyPair(keyPair);
        return rsaKeyPairManager;
    }
}
