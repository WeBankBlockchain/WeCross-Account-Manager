package com.webank.wecross.account.service.image.authcode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageAuthCodeManager {

    private static final Logger logger = LoggerFactory.getLogger(ImageAuthCodeManager.class);

    private final Map<String, ImageAuthCode> imageAuthCodeMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService = null;

    public ImageAuthCodeManager(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    cleanExpiredTokens();
                },
                30000,
                90000,
                TimeUnit.MILLISECONDS);
    }

    /** Clean up expired tokens */
    private void cleanExpiredTokens() {
        for (Map.Entry<String, ImageAuthCode> entry : imageAuthCodeMap.entrySet()) {
            String key = entry.getKey();
            ImageAuthCode pictureAuthCode = entry.getValue();
            if (pictureAuthCode.isExpired()) {
                logger.info("ImageAuthCode expired, value = {}", entry.getKey());
                imageAuthCodeMap.remove(key);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(" do cleanExpiredTokens ");
        }
    }

    public void add(ImageAuthCode pictureAuthCode) {
        imageAuthCodeMap.put(pictureAuthCode.getToken(), pictureAuthCode);
        logger.debug("add {}", pictureAuthCode);
    }

    public ImageAuthCode get(String token) {
        ImageAuthCode pictureAuthCode = imageAuthCodeMap.get(token);
        logger.debug("get token: {}", token);
        return pictureAuthCode;
    }

    public void remove(String token) {
        imageAuthCodeMap.remove(token);
        logger.debug("remove token: {}", token);
    }
}
