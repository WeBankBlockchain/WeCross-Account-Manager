package com.webank.wecross.account.service.db;

import com.webank.wecross.account.service.crypto.CryptoInterface;
import java.nio.charset.StandardCharsets;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class TokenSecKeyEntryConverter implements AttributeConverter<String, String> {
    private static Logger logger = LoggerFactory.getLogger(TokenSecKeyEntryConverter.class);

    private static CryptoInterface cryptoInterface = null;

    public static void initCryptoInterface(CryptoInterface cryptoInterface) {
        TokenSecKeyEntryConverter.cryptoInterface = cryptoInterface;
        logger.info(
                "[TokenSec] initCryptoInterface: {}", TokenSecKeyEntryConverter.cryptoInterface);
    }

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(String attribute) {

        if (cryptoInterface == null) {
            return attribute;
        }

        byte[] encode = cryptoInterface.encode(attribute.getBytes(StandardCharsets.UTF_8));
        String encodeString = new String(encode, StandardCharsets.UTF_8);
        return encodeString;
    }

    @SneakyThrows
    @Override
    public String convertToEntityAttribute(String dbData) {

        if (cryptoInterface == null) {
            return dbData;
        }

        try {
            byte[] decode = cryptoInterface.decode(dbData.getBytes(StandardCharsets.UTF_8));
            String decodeString = new String(decode, StandardCharsets.UTF_8);
            return decodeString;
        } catch (Exception e) {
            logger.warn(
                    "Failed to decrypt token_sec data and the raw data will be return, please check the password configuration[db:encryptKey]");
            logger.trace("e: ", e);
            return dbData;
        }
    }
}
