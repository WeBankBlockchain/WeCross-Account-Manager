package com.webank.wecross.account.service;

import com.webank.wecross.account.service.config.ApplicationConfigFile;
import com.webank.wecross.account.service.config.Default;
import org.junit.Test;

public class ApplicationConfigTest {
    @Test
    public void test() throws Exception {
        ApplicationConfigFile config = ApplicationConfigFile.parseFromFile(Default.CONFIG_FILE);
        System.out.println(config.toString());
    }
}
