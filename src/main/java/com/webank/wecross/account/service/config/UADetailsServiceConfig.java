package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.account.UADetailsService;
import com.webank.wecross.account.service.account.UAManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UADetailsServiceConfig {
    @Resource UAManager uaManager;

    @Bean
    public UADetailsService newUADetailsService() {
        return new UADetailsService(uaManager);
    }
}
