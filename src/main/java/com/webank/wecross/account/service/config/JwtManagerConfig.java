package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.authentication.JwtManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtManagerConfig {
    @Resource ApplicationConfig applicationConfig;

    @Bean
    public JwtManager newJwtManager() {
        JwtManager jwtManager = new JwtManager();
        jwtManager.setSecret(applicationConfig.getConfigFile().auth.secret);
        jwtManager.setIssuer(applicationConfig.getConfigFile().auth.secret);
        jwtManager.setExpires(applicationConfig.getConfigFile().auth.expires);
        return jwtManager;
    }
}
