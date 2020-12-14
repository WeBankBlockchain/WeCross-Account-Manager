package com.webank.wecross.account.service.exception;

public class UndefinedErrorException extends AccountManagerException {
    public UndefinedErrorException(String message) {
        super(ErrorCode.UndefinedError.getErrorCode(), message);
    }
}
