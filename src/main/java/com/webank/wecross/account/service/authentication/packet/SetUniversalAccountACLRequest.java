package com.webank.wecross.account.service.authentication.packet;

import lombok.Data;

@Data
public class SetUniversalAccountACLRequest {
    private String[] allowChainPaths;
}
