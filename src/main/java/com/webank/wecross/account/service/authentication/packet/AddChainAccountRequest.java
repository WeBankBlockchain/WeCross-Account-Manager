package com.webank.wecross.account.service.authentication.packet;

import lombok.Data;

@Data
public class AddChainAccountRequest {
    private String type;
    private String pubKey;
    private String secKey;
    private String ext;
    private Boolean isDefault = false;
}
