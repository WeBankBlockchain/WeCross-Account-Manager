package com.webank.wecross.account.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.service.crypto.CryptoInterface;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class RestRequestFilter {
    private static Logger logger = LoggerFactory.getLogger(RestRequestFilter.class);

    private Set<String> encryptURLSets = new HashSet<>();
    private CryptoInterface cryptoInterface;
    private ObjectMapper objectMapper = new ObjectMapper();

    public RestRequestFilter() {
        /** Add the encrypted data url */
        encryptURLSets.add("/auth/login");
        encryptURLSets.add("/auth/register");
        encryptURLSets.add("/auth/changePassword");

        /** ObjectMapper configure */
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        logger.info("URL encrypt sets: {}", Arrays.toString(encryptURLSets.toArray()));
    }

    public CryptoInterface getCryptoInterface() {
        return cryptoInterface;
    }

    public void setCryptoInterface(CryptoInterface cryptoInterface) {
        this.cryptoInterface = cryptoInterface;
    }

    /**
     * Determines whether the current request data is encrypted
     *
     * @param url
     * @return
     */
    public boolean isEncrypt(String url) {
        return encryptURLSets.contains(url);
    }

    /**
     * @param url
     * @param data
     * @param classz
     * @return
     * @throws Exception
     */
    public Object fetchRequestObject(String url, String data, Class<?> classz) throws Exception {

        if (isEncrypt(url)) {
            /** The requested data field is encrypted by RSA, decrypt the data */
            RestRequest<String> restRequest =
                    objectMapper.readValue(data, new TypeReference<RestRequest<String>>() {});
            byte[] params =
                    cryptoInterface.decode(restRequest.getData().getBytes(StandardCharsets.UTF_8));
            return objectMapper.readValue(params, classz);
        }

        JavaType javaType =
                objectMapper.getTypeFactory().constructParametricType(RestRequest.class, classz);
        RestRequest restRequest =
                objectMapper.readValue(data.getBytes(StandardCharsets.UTF_8), javaType);

        return restRequest.getData();
    }
}
