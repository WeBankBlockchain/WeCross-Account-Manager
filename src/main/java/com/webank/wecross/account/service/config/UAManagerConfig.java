package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.db.ChainAccountTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UAManagerConfig {
    @Autowired UniversalAccountTableJPA universalAccountTableJPA;

    @Autowired ChainAccountTableJPA chainAccountTableJPA;

    @Bean
    public UAManager newUAManager() throws AccountManagerException {
        UAManager uaManager = new UAManager();
        uaManager.setUniversalAccountTableJPA(universalAccountTableJPA);
        uaManager.setChainAccountTableJPA(chainAccountTableJPA);

        // TODO: delete mock user
        uaManager.addMockUA();

        return uaManager;
    }
}
