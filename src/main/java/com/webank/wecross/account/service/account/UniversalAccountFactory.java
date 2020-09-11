package com.webank.wecross.account.service.account;

import java.util.LinkedList;

public class UniversalAccountFactory {
    public static UniversalAccount newUA(String username, String password) {
        UniversalAccount ua = UniversalAccount.builder()
                .username(username)
                .uaID("uaud-" + username) // TODO:gen this
                .pubKey("pubkey-" + username) // TODO:gen this
                .password(password)
                .secKey(password) // TODO:gen this
                .role("User")
                .build();
        ua.setChainAccounts(new LinkedList<>());
        return ua;
    }
}
