package com.webank.wecross.account.service.exception;

public class JPAException extends AccountManagerException {
    public JPAException(String message) {
        super(ErrorCode.FlushDataException.getErrorCode(), message);
    }
}
