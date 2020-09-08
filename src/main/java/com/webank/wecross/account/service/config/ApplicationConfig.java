package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.exception.AccountManagerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    private ApplicationConfigFile configFile;

    ApplicationConfig() throws AccountManagerException {
        configFile = ApplicationConfigFile.parseFromFile(Default.CONFIG_FILE);
    }

    @Bean
    public ApplicationConfig newApplicationConfig() throws AccountManagerException {
        return new ApplicationConfig();
    }

    public ApplicationConfigFile getConfigFile() {
        return configFile;
    }
}
