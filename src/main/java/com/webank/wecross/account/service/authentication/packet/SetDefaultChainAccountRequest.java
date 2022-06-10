package com.webank.wecross.account.service.authentication.packet;
import lombok.Data;

@Data
public class SetDefaultChainAccountRequest {
    private String chainDefault;
    private Integer keyID; 
    
}
