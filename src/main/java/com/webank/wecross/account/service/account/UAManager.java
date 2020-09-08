package com.webank.wecross.account.service.account;

public class UAManager {

    public UAManager() {}

    public UniversalAccount getUA(String username) {
        // TODO: load from db
        UniversalAccount ua = new UniversalAccount();
        ua.setUsername(username);
        ua.setPassword("123456");
        ua.setRole("USER");
        return ua;
    }
}
