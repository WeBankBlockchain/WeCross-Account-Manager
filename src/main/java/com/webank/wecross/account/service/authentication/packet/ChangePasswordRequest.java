package com.webank.wecross.account.service.authentication.packet;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
    private String randomToken;
    private String authCode;
}
