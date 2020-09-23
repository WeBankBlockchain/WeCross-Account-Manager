package com.webank.wecross.account.service.authentication;

import com.webank.wecross.account.service.account.UAManager;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    public static final String TOKEN_HEADER = "Authorization";

    private JwtManager jwtManager;
    private UAManager uaManager;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtManager jwtManager,
            UAManager uaManager) {
        super(authenticationManager);
        this.jwtManager = jwtManager;
        this.uaManager = uaManager;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String tokenStringWithPrefix = request.getHeader(TOKEN_HEADER);
        try {
            if (!isJwtTokenWithPrefix(tokenStringWithPrefix)) {
                throw new Exception("Please login and use credential to access");
            }
            JwtToken token = jwtManager.verifyAndDecode(tokenStringWithPrefix);

            jwtManager.setCurrentLoginToken(token);
            uaManager.setCurrentLoginUA(token.getAudience());

            if (!jwtManager.hasLogout(token)) {
                jwtManager.setTokenActive(token);
            }

            // OK!
            chain.doFilter(request, response); // call restful API

        } catch (Exception e) {
            logger.error("Token authenticate failed: ", e);
            responseError(response, "Token authenticate failed: " + e.getMessage());
        }
    }

    private void responseError(HttpServletResponse response, String message) {
        try {
            response.setContentType("text/plain;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(message);

        } catch (Exception e) {
            logger.error("Response error failure " + e.getMessage());
        }
    }

    private boolean isJwtTokenWithPrefix(String tokenStringWithPrefix) {
        if (tokenStringWithPrefix != null
                && tokenStringWithPrefix.startsWith(JwtToken.TOKEN_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    public void setJwtManager(JwtManager jwtManager) {
        this.jwtManager = jwtManager;
    }

    public void setUaManager(UAManager uaManager) {
        this.uaManager = uaManager;
    }
}
