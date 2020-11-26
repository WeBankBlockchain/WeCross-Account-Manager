package com.webank.wecross.account.service.exception;

public class RequestParametersException extends AccountManagerException {
    public RequestParametersException(String message) {
        super(ErrorCode.InvalidParameters.getErrorCode(), message);
    }
}
