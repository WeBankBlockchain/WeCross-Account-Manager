package com.webank.wecross.account.service.exception;

public enum ErrorCode {
    Success(0),
    InvalidParameters(10001),
    UAAccountExist(10002),
    UAAccountNotExist(10003),
    ChainAccountExist(10004),
    ChainAccountNotExist(10005),
    ConfigurationItemError(10006),
    AccountOrPasswordIncorrect(10007),
    ImageAuthTokenExpired(10008),
    ImageAuthTokenNotExist(10009),
    ImageAuthTokenNotMatch(10010),
    NewUAException(10011),
    UserHasLogout(10012),
    ChainAccountTypeNotFound(10013),
    FlushDataException(10014),
    UndefinedError(10099);

    private int errorCode;

    ErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
