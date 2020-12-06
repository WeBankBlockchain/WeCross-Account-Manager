package com.webank.wecross.account.service.authcode;

import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ErrorCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthCodeManager {

    private static final Logger logger = LoggerFactory.getLogger(AuthCodeManager.class);

    private final Map<String, AuthCode> authCodeMap = new ConcurrentHashMap<>();
    private boolean allowImageAuthCodeEmpty = true;
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
            AuthCode authCode = entry.getValue();
            if (authCode.isExpired()) {
                logger.info("clear expired authCode, token: {}", entry.getKey());
                authCodeMap.remove(key);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(" do cleanExpiredTokens ");
        }
    }

    public void authToken(String randomToken) throws AccountManagerException {
        AuthCode authCode = getAuthCode(randomToken);
        if (authCode == null) {
            logger.error("token not exist, token:{}", randomToken);
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenNotExist.getErrorCode(), "auth token not found");
        }

        if (authCode.isExpired()) {
            logger.error("token expired, token: {}", authCode);
            removeAuthCode(randomToken);
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenExpired.getErrorCode(), "auth token has expired");
        }

        removeAuthCode(randomToken);
    }

    public void authToken(String randomToken, String imageCode) throws AccountManagerException {

        if (allowImageAuthCodeEmpty) {
            authToken(randomToken);
            return;
        }

        AuthCode authCode = getAuthCode(randomToken);
        if (authCode == null) {
            logger.error("token not exist, token:{}", randomToken);
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenNotExist.getErrorCode(), "image auth token not found");
        }

        if (authCode.isExpired()) {
            logger.error("image auth token expired, token: {}", authCode);
            removeAuthCode(randomToken);
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenExpired.getErrorCode(), "image auth token has expired");
        }

        if (!authCode.getCode().equalsIgnoreCase(imageCode)) {
            logger.error("image auth code not match, request: {}", authCode);
            removeAuthCode(randomToken);
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenNotMatch.getErrorCode(),
                    "image auth code does not match");
        }

        removeAuthCode(randomToken);
    }

    public void addAuthCode(AuthCode authCode) {
        authCodeMap.put(authCode.getToken(), authCode);
        if (logger.isDebugEnabled()) {
            logger.debug("add authCode: {}", authCode);
        }
    }

    public AuthCode getAuthCode(String token) {
        AuthCode authCode = authCodeMap.get(token);
        if (logger.isDebugEnabled()) {
            logger.debug("query authCode: {}", token);
        }
        return authCode;
    }

    public void removeAuthCode(String token) {
        authCodeMap.remove(token);
        if (logger.isDebugEnabled()) {
            logger.debug("remove authCode: {}", token);
        }
    }

    public boolean isAllowImageAuthCodeEmpty() {
        return allowImageAuthCodeEmpty;
    }

    public void setAllowImageAuthCodeEmpty(boolean allowImageAuthCodeEmpty) {
        this.allowImageAuthCodeEmpty = allowImageAuthCodeEmpty;
    }
}
