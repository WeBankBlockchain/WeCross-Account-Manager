package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.account.UAManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UAManagerConfig {

    @Bean
    public UAManager newUAManager() {
        return new UAManager();
    }
}
