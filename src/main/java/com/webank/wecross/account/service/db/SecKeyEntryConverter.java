package com.webank.wecross.account.service.db;

import com.webank.wecross.account.service.crypto.CryptoInterface;
import java.nio.charset.StandardCharsets;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class SecKeyEntryConverter implements AttributeConverter<String, String> {
    private static Logger logger = LoggerFactory.getLogger(SecKeyEntryConverter.class);

    private static CryptoInterface cryptoInterface = null;

    public static void initCryptoInterface(CryptoInterface cryptoInterface) {
        SecKeyEntryConverter.cryptoInterface = cryptoInterface;
        logger.info("initCryptoInterface: {}", SecKeyEntryConverter.cryptoInterface);
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
            logger.error(
                    "Failed to decrypt data, maybe the password is error, please check the [db:encryptKey] field configuration.");
            throw new RuntimeException(e.getCause());
        }
    }
}
