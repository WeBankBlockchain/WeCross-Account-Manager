package com.webank.wecross.account.service.authentication;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.service.RestRequestFilter;
import com.webank.wecross.account.service.RestResponse;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.account.UniversalAccount;
import com.webank.wecross.account.service.authcode.AuthCodeManager;
import com.webank.wecross.account.service.authentication.packet.LoginRequest;
import com.webank.wecross.account.service.authentication.packet.LoginResponse;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ErrorCode;
import com.webank.wecross.account.service.exception.RequestParametersException;
import com.webank.wecross.account.service.utils.PassWordUtility;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    private static Logger logger = LoggerFactory.getLogger(JwtLoginFilter.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    private String loginPath = "/auth/login"; // TODO: set from config
    private AuthenticationManager authenticationManager;
    private JwtManager jwtManager;
    private UAManager uaManager;
    private AuthCodeManager authCodeManager;
    private RestRequestFilter restRequestFilter;

    public JwtLoginFilter(
            AuthenticationManager authenticationManager,
            JwtManager jwtManager,
            UAManager uaManager,
            AuthCodeManager authCodeManager,
            RestRequestFilter restRequestFilter) {
        this.authenticationManager = authenticationManager;
        this.jwtManager = jwtManager;
        this.uaManager = uaManager;
        this.authCodeManager = authCodeManager;
        this.restRequestFilter = restRequestFilter;
        super.setFilterProcessesUrl(loginPath);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void setUaManager(UAManager uaManager) {
        this.uaManager = uaManager;
    }

    String getBodyString(HttpServletRequest request) throws Exception {
        BufferedReader br = request.getReader();
        String tmp, ret = "";
        while ((tmp = br.readLine()) != null) {
            ret += tmp;
        }
        return ret;
    }

    private LoginRequest parseLoginRequest(HttpServletRequest request) throws Exception {
        String body = getBodyString(request);

        LoginRequest loginRequest =
                (LoginRequest)
                        restRequestFilter.fetchRequestObject(
                                "/auth/login", body, LoginRequest.class);

        if (logger.isDebugEnabled()) {
            logger.debug("login params: {}", loginRequest);
        }

        if (loginRequest.getUsername() == null) {
            throw new RequestParametersException("username not found");
        }

        if (loginRequest.getPassword() == null) {
            throw new RequestParametersException("password not found");
        }

        if (loginRequest.getRandomToken() == null) {
            throw new RequestParametersException("random token not found");
        }

        //        if (loginRequest.getAuthCode() == null) {
        //            throw new RequestParametersException("image auth code not found");
        //        }

        logger.info("login request params: {}", loginRequest);

        return loginRequest;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {

            LoginRequest loginRequest = parseLoginRequest(request);

            String username = loginRequest.getUsername().trim();
            String password = loginRequest.getPassword().trim();
            String randomToken = loginRequest.getRandomToken().trim();
            String authCode = loginRequest.getAuthCode();

            // check randomToken and imageAuthCode
            authCodeManager.authToken(randomToken, authCode);

            if (!username.equals(uaManager.getAdminName())) {
                authCodeManager.authMailCode(username, loginRequest.getMailCode());
            }

            UniversalAccount ua = uaManager.getUA(username);

            logger.info("login username: {}", username);

            String mixedPassword = PassWordUtility.mixPassWithSalt(password, ua.getSalt());

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, mixedPassword));
        } catch (Exception e) {
            try {
                logger.error("Login exception: ", e);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/json;charset=utf-8");

                int errorCode = ErrorCode.AccountOrPasswordIncorrect.getErrorCode();
                if (e instanceof AccountManagerException) {
                    errorCode = ((AccountManagerException) e).getErrorCode();
                }

                LoginResponse loginResponse =
                        LoginResponse.builder()
                                .errorCode(errorCode)
                                .message("Login failed: " + e.getMessage())
                                .build();

                RestResponse restResponse =
                        RestResponse.builder()
                                .errorCode(0)
                                .message("success")
                                .data(loginResponse)
                                .build();

                response.getWriter().write(objectMapper.writeValueAsString(restResponse));
            } catch (Exception e1) {
                logger.error("e1: ", e1);
            }
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult)
            throws IOException {
        RestResponse restResponse = RestResponse.newSuccess();
        try {
            User user = (User) authResult.getPrincipal();
            String username = user.getUsername();

            JwtToken jwtToken = jwtManager.newToken(username);
            jwtManager.setTokenActive(jwtToken); // active it during login
            String tokenStr = jwtToken.getTokenStrWithPrefix(); // with prefix

            logger.info("Login success: username:{}", username);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/json;charset=utf-8");

            LoginResponse loginResponse =
                    LoginResponse.builder()
                            .errorCode(LoginResponse.SUCCESS)
                            .message("success")
                            .credential(tokenStr)
                            .universalAccount(uaManager.getUA(username).toInfo())
                            .build();

            restResponse.setData(loginResponse);

        } catch (AccountManagerException e) {
            LoginResponse loginResponse =
                    LoginResponse.builder()
                            .errorCode(e.getErrorCode())
                            .message(e.getMessage())
                            .credential(null)
                            .universalAccount(null)
                            .build();

            restResponse.setData(loginResponse);
        }
        response.getWriter().write(objectMapper.writeValueAsString(restResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed)
            throws IOException, ServletException {
        logger.info("Login failed", failed);

        LoginResponse loginResponse =
                LoginResponse.builder()
                        .errorCode(ErrorCode.AccountOrPasswordIncorrect.getErrorCode())
                        .message(failed.getMessage())
                        .build();

        RestResponse restResponse =
                RestResponse.builder().errorCode(0).message("success").data(loginResponse).build();

        String ret = objectMapper.writeValueAsString(restResponse);

        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(ret);
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }
}
