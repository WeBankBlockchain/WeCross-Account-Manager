package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.db.ChainAccountTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.image.authcode.ImageAuthCodeManager;
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
    public ImageAuthCodeManager newImageAuthCodeManager() {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("ImageAuthCode-"));
        ImageAuthCodeManager imageAuthCodeManager =
                new ImageAuthCodeManager(scheduledExecutorService);
        return imageAuthCodeManager;
    }
}
