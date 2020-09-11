package com.webank.wecross.account.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestResponse {
    private String version;
    private int errorCode;
    private String message;
    private Object data;

    public static RestResponse newSuccess() {
        return builder().version("1.0").errorCode(0).message("success").build();
    }

    public static RestResponse newFailed(String message) {
        return builder().version("1.0").errorCode(1).message("failed").build();
    }
}
