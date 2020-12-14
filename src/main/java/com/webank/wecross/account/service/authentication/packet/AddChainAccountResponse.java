package com.webank.wecross.account.service.authentication.packet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddChainAccountResponse {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    public int errorCode;
    public String message;
}
