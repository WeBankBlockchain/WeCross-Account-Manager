package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.authentication.JwtManager;

import javax.annotation.Resource;

import com.webank.wecross.account.service.db.LogoutTokenTableJPA;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtManagerConfig {
    @Resource
    ApplicationConfig applicationConfig;
    @Resource
    LogoutTokenTableJPA logoutTokenTableJPA;

    @Bean
    public JwtManager newJwtManager() {
        JwtManager jwtManager = new JwtManager();
        jwtManager.setSecret(applicationConfig.auth.secret);
        jwtManager.setIssuer(applicationConfig.auth.secret);
        jwtManager.setExpires(applicationConfig.auth.expires);

        jwtManager.setLogoutTokenTableJPA(logoutTokenTableJPA);
        return jwtManager;
    }
}
