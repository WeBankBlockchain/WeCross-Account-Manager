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

    private boolean needMailAuth = false;
    private final Map<String, AuthCode> authCodeMap = new ConcurrentHashMap<>();
    private final Map<String, MailCode> mailCodeMap = new ConcurrentHashMap<>();
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

        for (Map.Entry<String, MailCode> entry : mailCodeMap.entrySet()) {
            String key = entry.getKey();
            MailCode mailCode = entry.getValue();
            if (mailCode.isExpired()) {
                logger.info("clear expired mailCode for register, token: {}", entry.getKey());
                mailCodeMap.remove(key);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(" do cleanExpiredTokens ");
        }
    }

    private void authToken(String randomToken) throws AccountManagerException {
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

        /** Configure authentication codes not to be checked */
        if (allowImageAuthCodeEmpty && (imageCode == null || "".equals(imageCode.trim()))) {
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

    public void authMailCode(String username, String code) throws AccountManagerException {
        if (!isNeedMailAuth()) {
            return;
        }

        MailCode mailCode = mailCodeMap.get(username);
        if (mailCode == null) {
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenNotExist.getErrorCode(), "mail code not found");
        }

        if (mailCode.isExpired()) {
            mailCodeMap.remove(username);
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenExpired.getErrorCode(), "mail code token has expired");
        }

        if (!mailCode.getCode().equalsIgnoreCase(code)) {
            mailCodeMap.remove(username);
            throw new AccountManagerException(
                    ErrorCode.ImageAuthTokenNotMatch.getErrorCode(), "mail code does not match");
        }
        mailCodeMap.remove(username);
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

    public void addMailCode(String username, MailCode mailCode) {
        if (!isNeedMailAuth()) {
            return;
        }

        mailCodeMap.put(username, mailCode);
        if (logger.isDebugEnabled()) {
            logger.debug("add mailCode: {}", mailCode);
        }
    }

    public boolean isAllowImageAuthCodeEmpty() {
        return allowImageAuthCodeEmpty;
    }

    public void setAllowImageAuthCodeEmpty(boolean allowImageAuthCodeEmpty) {
        this.allowImageAuthCodeEmpty = allowImageAuthCodeEmpty;
    }

    public boolean isNeedMailAuth() {
        return needMailAuth;
    }

    public void setNeedMailAuth(boolean needMailAuth) {
        this.needMailAuth = needMailAuth;
    }
}
