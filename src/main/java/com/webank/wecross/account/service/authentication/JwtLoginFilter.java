package com.webank.wecross.account.service.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.service.MockRequest;
import com.webank.wecross.account.service.MockResponse;
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

    public JwtLoginFilter(AuthenticationManager authenticationManager, JwtManager jwtManager) {
        this.authenticationManager = authenticationManager;
        this.jwtManager = jwtManager;
        super.setFilterProcessesUrl(loginPath);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public class LoginReturn {
        private static final int SUCCESS = 0;
        private static final int ERROR = 1;

        public int errorCode;
        public String message;
        public String token;
    }

    String getBodyString(HttpServletRequest request) throws Exception {
        BufferedReader br = request.getReader();
        String tmp, ret = "";
        while ((tmp = br.readLine()) != null) {
            ret += tmp;
        }
        return ret;
    }

    public class LoginInfo {
        public String username;
        public String password;
    }

    private LoginInfo parseLoginRequest(HttpServletRequest request) throws Exception {
        String body = getBodyString(request);

        MockRequest loginInfoMap =
                objectMapper.readValue(body, new TypeReference<MockRequest>() {});

        LoginInfo loginInfo = new LoginInfo();
        loginInfo.username = (String) loginInfoMap.data.get("username");
        loginInfo.password = (String) loginInfoMap.data.get("password");

        if (loginInfo.username == null) {
            throw new Exception("username not found");
        }

        if (loginInfo.password == null) {
            throw new Exception("password not found");
        }

        return loginInfo;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {

            LoginInfo loginInfo = parseLoginRequest(request);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginInfo.username, loginInfo.password));
        } catch (Exception e) {
            try {
                logger.error("Login exception: " + e);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/json;charset=utf-8");

                MockResponse mockResponse = new MockResponse();

                LoginReturn loginReturn = new LoginReturn();
                loginReturn.errorCode = LoginReturn.ERROR;
                loginReturn.message = "Login failed: " + e.getMessage();
                mockResponse.data = loginReturn;

                response.getWriter().write(objectMapper.writeValueAsString(mockResponse));
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
        User user = (User) authResult.getPrincipal();
        String username = user.getUsername();
        JwtToken jwtToken = jwtManager.newToken(username);

        String tokenStr = jwtToken.getTokenStrWithPrefix();

        logger.info("Login success: name:{} token:{}", username, tokenStr);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json;charset=utf-8");

        MockResponse mockResponse = new MockResponse();

        LoginReturn loginReturn = new LoginReturn();
        loginReturn.errorCode = LoginReturn.SUCCESS;
        loginReturn.message = "success";
        loginReturn.token = tokenStr;

        mockResponse.data = loginReturn;
        response.getWriter().write(objectMapper.writeValueAsString(mockResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed)
            throws IOException, ServletException {
        logger.info("Login failed: {}", failed);

        String ret;

        MockResponse mockResponse = new MockResponse();

        LoginReturn loginReturn = new LoginReturn();
        loginReturn.errorCode = LoginReturn.ERROR;
        loginReturn.message = failed.getMessage();

        ret = objectMapper.writeValueAsString(mockResponse);

        response.setContentType("text/json;charset=utf-8");

        mockResponse.data = loginReturn;
        response.getWriter().write(ret);
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }
}
