package com.webank.wecross.account.service.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class JwtToken {
    public static final String TOKEN_PREFIX = "Bearer ";

    private String tokenStr;
    private DecodedJWT jwt;

    public JwtToken(String tokenStr) {
        this.tokenStr = tokenStr;
    }

    public String getIssuer() {
        prepareJwtCache();

        return jwt.getIssuer();
    }

    public String getAudience() {
        prepareJwtCache();

        return jwt.getAudience().get(0);
    }

    public Date getExpiresAt() {
        prepareJwtCache();

        return jwt.getExpiresAt();
    }

    public String getTokenStr() {
        return tokenStr;
    }

    public String getTokenStrWithPrefix() {
        return TOKEN_PREFIX + getTokenStr();
    }

    private void prepareJwtCache() {
        if (jwt == null) {
            jwt = JWT.decode(tokenStr);
        }
    }
}
