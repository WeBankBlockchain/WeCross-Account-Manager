package com.webank.wecross.account.service.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.account.service.config.Default;

public class BCOSChainAccount extends ChainAccount {
    public BCOSChainAccount() {
        super();
        super.setType(Default.BCOS_STUB_TYPE);
    }

    public String getAddress() {
        return super.identity;
    }

    public void setAddress(String address) {
        super.identity = address;
    }

    public String getPubKey() {
        return super.pubKey;
    }

    public void setPubKey(String pubKey) {
        super.pubKey = pubKey;
    }

    @JsonIgnore
    public String getSecKey() {
        return super.secKey;
    }

    public void setSecKey(String secKey) {
        super.secKey = secKey;
    }
}
