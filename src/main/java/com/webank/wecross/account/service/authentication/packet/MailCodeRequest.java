package com.webank.wecross.account.service.authentication.packet;

import lombok.Data;

@Data
public class MailCodeRequest {
    private String username;
    private String email;
}
