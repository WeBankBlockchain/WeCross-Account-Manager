package com.webank.wecross.account.service.exception;

public enum ErrorCode {
    Success(0),
    InvalidParameters(40001),
    UAAccountExist(40002),
    UAAccountNotExist(40003),
    ChainAccountExist(40004),
    ChainAccountNotExist(40005),
    ConfigurationItemError(40006),
    AccountOrPasswordIncorrect(40007),
    ImageAuthTokenExpired(40008),
    ImageAuthTokenNotExist(40009),
    ImageAuthTokenNotMatch(40010),
    CreateUAFailed(40011),
    UserHasLogout(40012),
    ChainAccountTypeNotFound(40013),
    FlushDataException(40014),
    ChainAccountHasBeenModified(40015),
    UAHasBeenModified(40016),
    InvalidPathFormat(40017),
    AccountExist(40018),
    MailNotFound(40019),
    UndefinedError(40099);

    private int errorCode;

    ErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
