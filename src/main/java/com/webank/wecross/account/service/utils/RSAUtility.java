package com.webank.wecross.account.service.utils;

import com.webank.wecross.account.service.exception.ConfigurationException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.Cipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSAUtility {
    private static Logger logger = LoggerFactory.getLogger(RSAUtility.class);

    /**
     * @param filename
     * @return
     */
    public static PrivateKey loadKeyPair(String filename)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
                    ConfigurationException {
        String content = FileUtility.readFileContent(filename);

        return null;
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        logger.info(
                "RSA pri: {}",
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        logger.info(
                "RSA Pub: {}",
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        return keyPair;
    }

    /**
     * @param
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] decryptBase64(String encryptedData, PrivateKey privateKey)
            throws Exception {
        byte[] decodeBase64 = Base64.getDecoder().decode(encryptedData);
        return decrypt(decodeBase64, privateKey);
    }

    /**
     * @param
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    /**
     * @param sourceData
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] sourceData, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(sourceData);
    }
}
