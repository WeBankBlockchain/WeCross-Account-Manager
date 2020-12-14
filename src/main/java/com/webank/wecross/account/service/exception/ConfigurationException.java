package com.webank.wecross.account.service.exception;

public class ConfigurationException extends AccountManagerException {
    public ConfigurationException(String message) {
        super(ErrorCode.ConfigurationItemError.getErrorCode(), message);
    }
}
