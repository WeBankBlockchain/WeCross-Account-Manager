package com.webank.wecross.account.service.authentication.packet;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String authCode;
    private String imageToken;
}
