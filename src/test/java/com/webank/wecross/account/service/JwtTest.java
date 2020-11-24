package com.webank.wecross.account.service;

import com.webank.wecross.account.service.authentication.JwtManager;
import com.webank.wecross.account.service.authentication.JwtToken;
import com.webank.wecross.account.service.config.JwtManagerConfig;
import java.util.Date;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JwtTest {
    @Resource JwtManagerConfig jwtManagerConfig;

    @Test
    public void verifyTest() throws Exception {
        JwtManager jwtManager = jwtManagerConfig.newJwtManager();
        JwtToken t1 = jwtManager.newToken("org1-admin");
        jwtManager.setTokenActive(t1);
        String t1String = t1.getTokenStr();

        System.out.println(t1String);

        jwtManager.verifyAndDecode(t1String);

        try {
            String fakeToken =
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ3ZWNyb3NzLWFjY291bnQtbWFuYWdlciJ9.cIEdBVUY15gG6tCqQxLcN7rg9BXkmOiGMqGMmPl1iAB";
            jwtManager.verifyAndDecode(fakeToken);
            Assert.assertTrue(false); // assume never comes here
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void decodeTest() throws Exception {
        String accountName = "org1-admin";
        JwtManager jwtManager = jwtManagerConfig.newJwtManager();
        JwtToken t1 = jwtManager.newToken(accountName);
        jwtManager.setTokenActive(t1);
        String t1String = t1.getTokenStr();

        System.out.println(t1String);

        JwtToken t2 = jwtManager.verifyAndDecode(t1String);
        Assert.assertEquals(accountName, t2.getAudience());
    }

    @Test
    public void expiresTest() throws Exception {
        String accountName = "org1-admin";
        JwtManager jwtManager = jwtManagerConfig.newJwtManager();
        jwtManager.setExpires(new Long(1)); // expires is 1s
        JwtToken t1 = jwtManager.newToken(accountName);
        jwtManager.setTokenActive(t1);
        String t1String = t1.getTokenStr();

        System.out.println("time 0" + (new Date()).toString() + " jwt " + t1String);
        Thread.sleep(200); // sleep 0.5s
        System.out.println("time 1 " + (new Date()).toString());
        jwtManager.verifyAndDecode(t1String); // verify ok
        System.out.println("time 2 " + (new Date()).toString());

        Thread.sleep(2000); // sleep 2s
        System.out.println("time 3 " + (new Date()).toString());
        try {
            JwtToken token = jwtManager.verifyAndDecode(t1String); // verify failed
            System.out.println("time 4 " + token.getExpiresAt());
            Assert.assertTrue(false); // assume never comes here
        } catch (Exception e) {
            System.out.println("OK: " + e.getMessage());
            Assert.assertTrue(true);
        }
    }

    @Test
    public void concurrentTest() throws Exception {

        JwtManager jwtManager = jwtManagerConfig.newJwtManager();
        JwtToken token = jwtManager.newToken("aaa");
        for (int i = 0; i < 10; i++) {
            JwtToken newToken = jwtManager.newToken("aaa");
            Assert.assertNotEquals(token, newToken);
            token = newToken;
        }
    }
}
