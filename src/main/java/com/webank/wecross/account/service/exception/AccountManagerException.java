package com.webank.wecross.account.service.exception;

public class AccountManagerException extends Exception {
    public AccountManagerException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "(" + this.getClass().getSimpleName() + "):" + super.getMessage();
    }
}
