package com.webank.wecross.account.service.authcode;

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

public class ImageCodeCreator {
    private static final Logger logger = LoggerFactory.getLogger(ImageCodeCreator.class);

    /** Image authentication code validity period */
    public static final int Image_Auth_Code_Validity_Time = 300;
    /** Image authentication code length */
    public static final int Image_Auth_Code_Char_Number = 4;

    public static final int MAIL_CODE_VALIDITY_TINE = 180; // seconds
    public static final int MAIL_CODE_LENGTH = 6;

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
    public static AuthCode createAuthCode() throws NoSuchAlgorithmException, IOException {
        AuthCode imageAuthCode = new AuthCode();

        String value = UUID.randomUUID().toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String token = Hex.toHexString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        imageAuthCode.setToken(token);
        imageAuthCode.setValidTime(Image_Auth_Code_Validity_Time);
        imageAuthCode.setCreateTime(LocalDateTime.now());
        imageAuthCode.setCode(randomString(Image_Auth_Code_Char_Number));
        imageAuthCode.setImageBase64(TokenImgGenerator.getBase64Image(imageAuthCode.getCode()));

        logger.info("new AuthCode, {}", imageAuthCode);

        return imageAuthCode;
    }

    public static MailCode getMailCode() {
        MailCode mailCode = new MailCode();
        mailCode.setValidTime(MAIL_CODE_VALIDITY_TINE);
        mailCode.setCreateTime(LocalDateTime.now());
        mailCode.setCode(genRandomMailCode(MAIL_CODE_LENGTH));

        logger.info("new mailCode, {}", mailCode);
        return mailCode;
    }

    public static String genRandomMailCode(int length) {
        char[] arr = new char[length];
        int i = 0;
        while (i < length) {
            char ch = (char) (int) (Math.random() * 124);
            if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                arr[i++] = ch;
            }
        }
        return new String(arr);
    }
}
