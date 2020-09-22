package com.webank.wecross.account.service.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.webank.wecross.account.service.db.LogoutTokenTableBean;
import com.webank.wecross.account.service.db.LogoutTokenTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.JPAException;
import com.webank.wecross.account.service.exception.LogoutException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtManager {

    private static Logger logger = LoggerFactory.getLogger(JwtManager.class);

    private LogoutTokenTableJPA logoutTokenTableJPA;

    private String secret;
    private String issuer;
    private Long expires;

    private ThreadLocal<JwtToken> currentLoginToken = new ThreadLocal<>();

    public JwtManager() {}

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        JwtManager.logger = logger;
    }

    public JwtToken newToken(String accountName) {
        JwtToken token = null;
        try {
            Date startTime = new Date();
            Date expiresTime = toExpiresDate(startTime);

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
        } catch (Exception exception) {
            // Invalid Signing configuration / Couldn't convert Claims.
            logger.error("new Token error, name:" + accountName);
        }
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

        check(tokenWithoutPrefix);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier =
                JWT.require(algorithm)
                        .withIssuer(issuer)
                        // .acceptLeeway(expires)
                        .build(); // Reusable verifier instance
        verifier.verify(tokenWithoutPrefix);
    }

    private void check(String tokenStr) throws AccountManagerException {
        if (hasLogoutToken(tokenStr)) {
            throw new LogoutException("user has logged out");
        }
    }

    public boolean hasLogoutToken(String tokenStr) {
        LogoutTokenTableBean tableBean = logoutTokenTableJPA.findByToken(tokenStr);
        return Objects.nonNull(tableBean);
    }

    public void setLogoutToken(String tokenStr) throws JPAException {
        LogoutTokenTableBean tableBean = new LogoutTokenTableBean();
        tableBean.setToken(tokenStr);
        if (Objects.isNull(logoutTokenTableJPA.saveAndFlush(tableBean))) {
            throw new JPAException("logout failed");
        }
    }

    public void setSecret(String secret) {
        this.secret = secret;
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

    public void setLogoutTokenTableJPA(LogoutTokenTableJPA logoutTokenTableJPA) {
        this.logoutTokenTableJPA = logoutTokenTableJPA;
    }

    public JwtToken getCurrentLoginToken() {
        return currentLoginToken.get();
    }

    public void setCurrentLoginToken(JwtToken currentLoginToken) {
        this.currentLoginToken.set(currentLoginToken);
    }
}
