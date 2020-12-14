package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.exception.AccountManagerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfigConfig {
    @Bean
    public ApplicationConfig newApplicationConfig() throws AccountManagerException {
        return ApplicationConfig.parseFromFile(Default.CONFIG_FILE);
    }
}
