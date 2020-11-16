package com.webank.wecross.account.service.image.authcode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageAuthCodeCreator {
    private static final Logger logger = LoggerFactory.getLogger(ImageAuthCodeCreator.class);

    /** Image authentication code validity period */
    public static final int Picture_Auth_Code_Validity_Time = 90;
    /** Image authentication code length */
    public static final int Picture_Auth_Code_Char_Number = 4;

    /** */
    private static final char[] CHARS = {
        '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z'
    };

    /**
     * Gets a numeric and alphanumeric string for the specified number of digits
     *
     * @param length
     */
    public static String randomString(int length) {
        if (length > CHARS.length) {
            return null;
        }
        Random random = new Random();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static ImageAuthCode createImageAuthCode() throws NoSuchAlgorithmException, IOException {
        ImageAuthCode imageAuthCode = new ImageAuthCode();

        String value = UUID.randomUUID().toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String token = Hex.toHexString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        imageAuthCode.setToken(token);
        imageAuthCode.setValidTime(Picture_Auth_Code_Validity_Time);
        imageAuthCode.setCreateTime(LocalDateTime.now());
        imageAuthCode.setCode(randomString(Picture_Auth_Code_Char_Number));
        imageAuthCode.setImageBase64(TokenImgGenerator.getBase64Image(imageAuthCode.getCode()));

        logger.info("new ImageAuthCode, {}", imageAuthCode);

        return imageAuthCode;
    }
}
