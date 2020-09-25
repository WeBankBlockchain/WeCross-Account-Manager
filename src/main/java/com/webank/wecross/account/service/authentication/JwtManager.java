package com.webank.wecross.account.service.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.webank.wecross.account.service.db.LoginTokenTableBean;
import com.webank.wecross.account.service.db.LoginTokenTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.JPAException;
import com.webank.wecross.account.service.exception.LogoutException;
import com.webank.wecross.account.service.exception.UANotFoundException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtManager {

    private static Logger logger = LoggerFactory.getLogger(JwtManager.class);

    private UniversalAccountTableJPA universalAccountTableJPA;
    private LoginTokenTableJPA loginTokenTableJPA;

    private String issuer;
    private Long expires;
    private Long noActiveExpires;

    private ThreadLocal<JwtToken> currentLoginToken = new ThreadLocal<>();

    public JwtManager() {}

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        JwtManager.logger = logger;
    }

    public JwtToken newToken(String accountName) throws AccountManagerException {
        JwtToken token = null;

        Date startTime = new Date();
        Date expiresTime = toExpiresDate(startTime);
        String secret = getUATokenSec(accountName);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        String tokenStr =
                JWT.create()
                        .withIssuer(issuer)
                        .withAudience(accountName)
                        .withIssuedAt(startTime)
                        .withNotBefore(startTime)
                        .withExpiresAt(expiresTime)
                        .sign(algorithm);
        token = new JwtToken(tokenStr);

        return token;
    }

    public JwtToken verifyAndDecode(String tokenStr) throws AccountManagerException {
        String tokenWithoutPrefix =
                tokenStr.replaceAll(JwtToken.TOKEN_PREFIX, "").replaceAll(" ", "");

        verify(tokenWithoutPrefix);

        return new JwtToken(tokenWithoutPrefix);
    }

    public void verify(String tokenStr) throws AccountManagerException {
        String tokenWithoutPrefix =
                tokenStr.replaceAll(JwtToken.TOKEN_PREFIX, "").replaceAll(" ", "");
        JwtToken token = decode(tokenStr);
        String accountName = token.getAudience();
        String secret = getUATokenSec(accountName);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier =
                JWT.require(algorithm)
                        .withIssuer(issuer)
                        // .acceptLeeway(expires)
                        .build(); // Reusable verifier instance
        verifier.verify(tokenWithoutPrefix);
        check(tokenStr);
    }

    public JwtToken decode(String tokenStr) throws AccountManagerException {
        String tokenWithoutPrefix =
                tokenStr.replaceAll(JwtToken.TOKEN_PREFIX, "").replaceAll(" ", "");
        return new JwtToken(tokenWithoutPrefix);
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }

    private Date toExpiresDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, expires.intValue());
        return cal.getTime();
    }

    private void check(String tokenStr) throws AccountManagerException {
        if (hasLogout(tokenStr)) {
            throw new LogoutException("user has logged out");
        }
    }

    public boolean hasLogout(String tokenStr) {
        long lastActive = getLastActiveTimestamp(tokenStr);
        long current = System.currentTimeMillis();
        return current - lastActive > noActiveExpires.longValue() * 1000;
    }

    public boolean hasLogout(JwtToken token) {
        if (token == null) {
            return true;
        }
        return hasLogout(token.getTokenStr());
    }

    public long getLastActiveTimestamp(String tokenStr) {
        LoginTokenTableBean tableBean = loginTokenTableJPA.findByToken(tokenStr);
        if (tableBean == null) {
            return 0; // never active
        }
        return tableBean.getLastActiveTimestamp();
    }

    public void setLogoutToken(String tokenStr) throws JPAException {
        LoginTokenTableBean tableBean = loginTokenTableJPA.findByToken(tokenStr);
        if (tableBean == null) {
            tableBean = new LoginTokenTableBean();
            tableBean.setToken(tokenStr);
        }
        tableBean.setLogout();
        if (Objects.isNull(loginTokenTableJPA.saveAndFlush(tableBean))) {
            throw new JPAException("logout failed");
        }
    }

    public void setTokenActive(String tokenStr) throws JPAException {
        LoginTokenTableBean tableBean = loginTokenTableJPA.findByToken(tokenStr);
        if (tableBean == null) {
            tableBean = new LoginTokenTableBean();
            tableBean.setToken(tokenStr);
        }
        tableBean.setLastActiveTimestamp(System.currentTimeMillis());
        if (Objects.isNull(loginTokenTableJPA.saveAndFlush(tableBean))) {
            throw new JPAException("setTokenActive failed");
        }
    }

    public void setTokenActive(JwtToken token) throws JPAException {
        setTokenActive(token.getTokenStr());
    }

    public JwtToken getCurrentLoginToken() {
        return currentLoginToken.get();
    }

    public void setCurrentLoginToken(JwtToken currentLoginToken) {
        this.currentLoginToken.set(currentLoginToken);
    }

    public void setUniversalAccountTableJPA(UniversalAccountTableJPA universalAccountTableJPA) {
        this.universalAccountTableJPA = universalAccountTableJPA;
    }

    private String getUATokenSec(String accountName) throws AccountManagerException {
        String tokenSec = universalAccountTableJPA.findTokenSecByUsername(accountName);
        if (tokenSec == null || tokenSec.length() == 0) {
            throw new UANotFoundException("account " + accountName + " not found");
        }

        return tokenSec;
    }

    public void setLoginTokenTableJPA(LoginTokenTableJPA loginTokenTableJPA) {
        this.loginTokenTableJPA = loginTokenTableJPA;
    }

    public void setNoActiveExpires(Long noActiveExpires) {
        this.noActiveExpires = noActiveExpires;
    }
}
