package com.webank.wecross.account.service.authentication.packet;

import lombok.Data;

@Data
public class SetDefaultAccountRequest {
    private String type;
    private Integer keyID;
}
