package com.webank.wecross.account.service.authentication.packet;
import lombok.Data;

@Data
public class SetDefaultFabricAccountRequest {
    private String fabricDefault;
    private Integer keyID; 
    
}
