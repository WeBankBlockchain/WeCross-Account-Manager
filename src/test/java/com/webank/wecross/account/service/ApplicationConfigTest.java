package com.webank.wecross.account.service;

import com.webank.wecross.account.service.config.ApplicationConfig;
import com.webank.wecross.account.service.config.Default;
import org.junit.Test;

public class ApplicationConfigTest {
    @Test
    public void test() throws Exception {
        ApplicationConfig config = ApplicationConfig.parseFromFile(Default.CONFIG_FILE);
        System.out.println(config.toString());
    }
}
