package com.webank.wecross.account.service.authentication.packet;

import com.webank.wecross.account.service.account.UniversalAccount;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    public int errorCode;
    public String message;
    public String credential;
    public UniversalAccount.Info universalAccount;
}
