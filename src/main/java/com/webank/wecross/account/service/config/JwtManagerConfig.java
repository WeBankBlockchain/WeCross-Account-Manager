package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.authentication.JwtManager;
import com.webank.wecross.account.service.db.LogoutTokenTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtManagerConfig {
    @Resource ApplicationConfig applicationConfig;
    @Resource UniversalAccountTableJPA universalAccountTableJPA;
    @Resource LogoutTokenTableJPA logoutTokenTableJPA;

    @Bean
    public JwtManager newJwtManager() {
        JwtManager jwtManager = new JwtManager();
        jwtManager.setIssuer(applicationConfig.auth.name);
        jwtManager.setExpires(applicationConfig.auth.expires);

        jwtManager.setUniversalAccountTableJPA(universalAccountTableJPA);
        jwtManager.setLogoutTokenTableJPA(logoutTokenTableJPA);
        return jwtManager;
    }
}
