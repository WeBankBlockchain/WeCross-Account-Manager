package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.ServiceContext;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.authentication.JwtManager;
import com.webank.wecross.account.service.image.authcode.ImageAuthCodeManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceContextConfig {
    @Resource ServiceConfig serviceConfig;
    @Resource JwtManager jwtManager;
    @Resource UAManager uaManager;
    @Resource ImageAuthCodeManager imageAuthCodeManager;

    @Bean
    public ServiceContext newServiceContext() {
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setServiceConfig(serviceConfig);
        serviceContext.setJwtManager(jwtManager);
        serviceContext.setUaManager(uaManager);
        serviceContext.setImageAuthCodeManager(imageAuthCodeManager);
        return serviceContext;
    }
}
