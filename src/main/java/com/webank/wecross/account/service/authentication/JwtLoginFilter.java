package com.webank.wecross.account.service.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.service.RestRequest;
import com.webank.wecross.account.service.RestResponse;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.account.UniversalAccount;
import com.webank.wecross.account.service.authentication.packet.LoginRequest;
import com.webank.wecross.account.service.authentication.packet.LoginResponse;
import com.webank.wecross.account.service.exception.AccountManagerException;
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

    public JwtLoginFilter(
            AuthenticationManager authenticationManager,
            JwtManager jwtManager,
            UAManager uaManager) {
        this.authenticationManager = authenticationManager;
        this.jwtManager = jwtManager;
        this.uaManager = uaManager;
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

        RestRequest<LoginRequest> restRequest =
                objectMapper.readValue(body, new TypeReference<RestRequest<LoginRequest>>() {});

        LoginRequest loginRequest = restRequest.getData();

        if (loginRequest.getUsername() == null) {
            throw new Exception("username not found");
        }

        if (loginRequest.getPassword() == null) {
            throw new Exception("password not found");
        }

        return loginRequest;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {

            LoginRequest loginRequest = parseLoginRequest(request);

            UniversalAccount ua = uaManager.getUA(loginRequest.getUsername());
            logger.info(
                    "username: {}, password: {}, salt: {}",
                    loginRequest.getUsername(),
                    loginRequest.getPassword(),
                    ua.getSalt());
            String username = loginRequest.getUsername();
            String password =
                    PassWordUtility.mixPassWithSalt(loginRequest.getPassword(), ua.getSalt());

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            try {
                logger.error("Login exception: ", e);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/json;charset=utf-8");

                LoginResponse loginResponse =
                        LoginResponse.builder()
                                .errorCode(LoginResponse.ERROR)
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
                logger.error(e1.getMessage());
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

            logger.info("Login success: username:{} credential:{}", username, tokenStr);

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
                            .errorCode(LoginResponse.ERROR)
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
        logger.info("Login failed: {}", failed);

        String ret;

        LoginResponse loginResponse =
                LoginResponse.builder()
                        .errorCode(LoginResponse.ERROR)
                        .message(failed.getMessage())
                        .build();

        RestResponse restResponse =
                RestResponse.builder().errorCode(0).message("success").data(loginResponse).build();

        ret = objectMapper.writeValueAsString(restResponse);

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
