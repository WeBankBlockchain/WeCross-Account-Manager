package com.webank.wecross.account.service.authentication.packet;

import lombok.Data;

@Data
public class ModifyPasswordRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
}
