package com.webank.wecross.account.service.authentication.packet;

import com.webank.wecross.account.service.account.UniversalAccount;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    public int errorCode;
    public String message;

    public UniversalAccount.Info universalAccount;
}
