package com.webank.wecross.account.service.utils;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSAUtility {

    private static Logger logger = LoggerFactory.getLogger(RSAUtility.class);

    /**
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair newKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        logger.info(
                "RSA Pub: {}",
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        return keyPair;
    }

    /**
     * @param content
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey createPublicKey(String content)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PemReader pemReader = new PemReader(new StringReader(content));
        PemObject pemObject = pemReader.readPemObject();
        byte[] pemContent = pemObject.getContent();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(pemContent);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * @param content
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey createPrivateKey(String content)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PemReader pemReader = new PemReader(new StringReader(content));
        PemObject pemObject = pemReader.readPemObject();
        byte[] pemContent = pemObject.getContent();
        RSAPrivateKey rsaPrivateKey =
                RSAPrivateKey.getInstance(ASN1Primitive.fromByteArray(pemContent));
        RSAPrivateKeySpec rsaPrivateKeySpec =
                new RSAPrivateKeySpec(
                        rsaPrivateKey.getModulus(), rsaPrivateKey.getPrivateExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(rsaPrivateKeySpec);
    }
}
