package com.webank.wecross.account.service.exception;

public class AccountManagerException extends Exception {
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public AccountManagerException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return "(" + this.getClass().getSimpleName() + "):" + super.getMessage();
    }
}
