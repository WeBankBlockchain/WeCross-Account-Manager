package com.webank.wecross.account.service.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtManager {

    private static Logger logger = LoggerFactory.getLogger(JwtManager.class);

    private String secret;
    private String issuer;
    private Long expires;

    public JwtManager() {}

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

    public JwtToken decode(String tokenStr) throws Exception {
        String tokenWithoutPrefix =
                tokenStr.replaceAll(JwtToken.TOKEN_PREFIX, "").replaceAll(" ", "");

        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier =
                JWT.require(algorithm)
                        .withIssuer(issuer)
                        // .acceptLeeway(expires)
                        .build(); // Reusable verifier instance
        verifier.verify(tokenWithoutPrefix);

        return new JwtToken(tokenWithoutPrefix);
    }

    public void verify(String tokenStr) throws Exception {
        String tokenWithoutPrefix =
                tokenStr.replaceAll(JwtToken.TOKEN_PREFIX, "").replaceAll(" ", "");
        ;

        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier =
                JWT.require(algorithm)
                        .withIssuer(issuer)
                        // .acceptLeeway(expires)
                        .build(); // Reusable verifier instance
        verifier.verify(tokenWithoutPrefix);
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
}
