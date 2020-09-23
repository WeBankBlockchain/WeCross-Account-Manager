package com.webank.wecross.account.service.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.UANotFoundException;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtManager {

    private static Logger logger = LoggerFactory.getLogger(JwtManager.class);

    private UniversalAccountTableJPA universalAccountTableJPA;

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
}
