package com.webank.wecross.account.service;

import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.authentication.JwtManager;
import com.webank.wecross.account.service.config.ServiceConfig;
import com.webank.wecross.account.service.image.authcode.ImageAuthCodeManager;
import lombok.Data;

@Data
public class ServiceContext {
    private ServiceConfig serviceConfig;
    private JwtManager jwtManager;
    private UAManager uaManager;
    private ImageAuthCodeManager imageAuthCodeManager;
}
