package com.webank.wecross.account.service.authentication.packet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PubResponse {
    public int errorCode;
    public String message;
    public String pub;
}
