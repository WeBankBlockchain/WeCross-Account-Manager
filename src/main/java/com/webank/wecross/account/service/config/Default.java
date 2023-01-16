package com.webank.wecross.account.service.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Default {
    public static final List<String> SUPPORT_STUB_TYPE = new ArrayList<>();
    public static final String CONFIG_FILE = "classpath:application.toml";

    public static final String BCOS_STUB_KEY_WORD = "BCOS";
    public static final String BCOS_STUB_TYPE = "BCOS2.0";
    public static final String BCOS_3_STUB_TYPE = "BCOS3_ECDSA_EVM";
    public static final String BCOS_GM_STUB_TYPE = "GM_BCOS2.0";
    public static final String BCOS_3_GM_STUB_TYPE = "BCOS3_GM_EVM";
    public static final String FABRIC_STUB_KEY_WORD = "Fabric";
    public static final String FABRIC_STUB_TYPE = "Fabric1.4";
    public static final String FABRIC_STUB_TYPE2 = "Fabric2.0";

    private static final List<String> FIELD_NAME_FILTER = Arrays.asList("KEY_WORD", "CONFIG");

    static {
        Field[] declaredFields = Default.class.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.getType().equals(String.class)
                    && !FIELD_NAME_FILTER.contains(declaredField.getName())) {
                try {
                    SUPPORT_STUB_TYPE.add((String) declaredField.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
