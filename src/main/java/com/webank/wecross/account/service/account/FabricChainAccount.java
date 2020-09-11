package com.webank.wecross.account.service.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.account.service.config.Default;

public class FabricChainAccount extends ChainAccount {
    public FabricChainAccount() {
        super();
        super.setType(Default.FABRIC_STUB_TYPE);
    }

    public String getCert() {
        return super.pubKey;
    }

    public void setCert(String cert) {
        super.pubKey = cert;
    }

    @JsonIgnore
    public String getKey() {
        return  super.secKey;
    }

    public void setKey(String key) {
        super.secKey = key;
    }
}
