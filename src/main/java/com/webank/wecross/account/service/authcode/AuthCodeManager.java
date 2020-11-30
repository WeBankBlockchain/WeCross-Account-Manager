package com.webank.wecross.account.service.authcode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthCodeManager {

    private static final Logger logger = LoggerFactory.getLogger(AuthCodeManager.class);

    private final Map<String, AuthCode> authCodeMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService = null;

    public AuthCodeManager(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    cleanExpiredCodes();
                },
                30000,
                90000,
                TimeUnit.MILLISECONDS);
    }

    /** Clean up expired tokens */
    private void cleanExpiredCodes() {
        for (Map.Entry<String, AuthCode> entry : authCodeMap.entrySet()) {
            String key = entry.getKey();
            AuthCode pictureAuthCode = entry.getValue();
            if (pictureAuthCode.isExpired()) {
                logger.info("AuthCode expired, value = {}", entry.getKey());
                authCodeMap.remove(key);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(" do cleanExpiredTokens ");
        }
    }

    public void add(AuthCode authCode) {
        authCodeMap.put(authCode.getToken(), authCode);
        logger.debug("add {}", authCode);
    }

    public AuthCode get(String token) {
        AuthCode authCode = authCodeMap.get(token);
        logger.debug("get token: {}", token);
        return authCode;
    }

    public void remove(String token) {
        authCodeMap.remove(token);
        logger.debug("remove token: {}", token);
    }
}
