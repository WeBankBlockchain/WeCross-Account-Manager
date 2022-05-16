package com.webank.wecross.account.service;

import com.webank.wecross.account.service.exception.AccountManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler({Exception.class})
    public RestResponse handleException(Exception e) {
        logger.error("failed to handle restful request: ", e);
        return RestResponse.newFailed("undefined error");
    }

    @ExceptionHandler({RuntimeException.class})
    public RestResponse handleRuntimeException(RuntimeException e) {
        logger.error("failed to handle restful request: ", e);
        return RestResponse.newFailed("runtime error");
    }

    @ExceptionHandler(value = Throwable.class)
    public RestResponse HandleThrowable(Throwable e) {
        logger.error("failed to handle restful request: ", e);
        return RestResponse.newFailed("throwable error");
    }

    @ExceptionHandler({AccountManagerException.class})
    public RestResponse handleAuthServiceException(AccountManagerException e) {
        logger.error("failed to handle restful request: {}", e.getMessage());
        return RestResponse.builder().errorCode(e.getErrorCode()).message(e.getMessage()).build();
    }
}
