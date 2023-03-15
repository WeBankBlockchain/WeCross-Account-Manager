package com.webank.wecross.account.service;

import com.webank.wecross.account.service.account.AccountAccessControlList;
import com.webank.wecross.account.service.account.ChainAccount;
import com.webank.wecross.account.service.account.ChainAccountBuilder;
import com.webank.wecross.account.service.account.LoginSalt;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.account.UniversalAccount;
import com.webank.wecross.account.service.account.UniversalAccountBuilder;
import com.webank.wecross.account.service.authcode.*;
import com.webank.wecross.account.service.authentication.JwtManager;
import com.webank.wecross.account.service.authentication.JwtToken;
import com.webank.wecross.account.service.authentication.packet.*;
import com.webank.wecross.account.service.config.ApplicationConfig;
import com.webank.wecross.account.service.crypto.CryptoRSABase64Impl;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ErrorCode;
import com.webank.wecross.account.service.exception.RequestParametersException;
import com.webank.wecross.account.service.utils.PassWordUtility;
import com.webank.wecross.account.service.utils.Path;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServiceController {
    private static Logger logger = LoggerFactory.getLogger(ServiceController.class);

    public ServiceController() {}

    @Resource ServiceContext serviceContext;
    @Resource ApplicationConfig applicationConfig;

    @RequestMapping("/test")
    private Object test() {
        return "OK!";
    }

    private void checkChangePasswordRequest(ChangePasswordRequest request)
            throws AccountManagerException {
        if (request.getUsername() == null) {
            throw new RequestParametersException("username has not given");
        }

        if (request.getUsername().length() > 256) {
            throw new RequestParametersException("username is too long, limit 256");
        }

        if (request.getOldPassword().length() > 256) {
            throw new RequestParametersException("old password is too long, limit 256");
        }

        if (request.getNewPassword().length() > 256) {
            throw new RequestParametersException("new password is too long, limit 256");
        }

        if (request.getRandomToken() == null) {
            throw new RequestParametersException("random token has not given");
        }
    }

    private void checkRegisterRequest(RegisterRequest request) throws AccountManagerException {
        if (serviceContext.getAuthCodeManager().isNeedMailAuth()) {
            if (request.getEmail() == null) {
                throw new RequestParametersException("email has not given");
            }
            if (request.getMailCode() == null) {
                throw new RequestParametersException("mail code has not given");
            }
        }

        if (request.getUsername() == null) {
            throw new RequestParametersException("username has not given");
        }

        if (request.getPassword() == null) {
            throw new RequestParametersException("password has not given");
        }

        if (request.getRandomToken() == null) {
            throw new RequestParametersException("random token has not given");
        }

        /*
        if (request.getAuthCode() == null) {
            throw new RequestParametersException("image auth code has not given");
        }
        */

        if (request.getUsername().length() > 256) {
            throw new RequestParametersException("username is too long, limit 256");
        }

        if (request.getPassword().length() > 256) {
            throw new RequestParametersException("password is too long, limit 256");
        }
    }

    @RequestMapping(
            value = "/auth/changePassword",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object changePassword(@RequestBody String params) {
        RestResponse restResponse = RestResponse.newSuccess();
        try {

            ChangePasswordRequest changePasswordRequest =
                    (ChangePasswordRequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/changePassword",
                                            params,
                                            ChangePasswordRequest.class);

            logger.info("ChangePasswordRequest: {}", changePasswordRequest);

            checkChangePasswordRequest(changePasswordRequest);

            serviceContext
                    .getAuthCodeManager()
                    .authToken(
                            changePasswordRequest.getRandomToken(),
                            changePasswordRequest.getAuthCode());

            UniversalAccount ua =
                    serviceContext.getUaManager().getUA(changePasswordRequest.getUsername());
            String passwordWithSalt =
                    PassWordUtility.mixPassWithSalt(
                            changePasswordRequest.getOldPassword(), ua.getSalt());
            if (!passwordWithSalt.equals(ua.getPassword())) {
                throw new AccountManagerException(
                        ErrorCode.AccountOrPasswordIncorrect.getErrorCode(),
                        "account or password incorrect");
            }

            String newSalt = UUID.randomUUID().toString();
            ua.setPassword(
                    PassWordUtility.mixPassWithSalt(
                            changePasswordRequest.getNewPassword(), newSalt));
            ua.setSalt(newSalt);

            // update password
            serviceContext.getUaManager().setUA(ua);

            ModifyPasswordResponse modifyPasswordResponse =
                    ModifyPasswordResponse.builder().errorCode(0).message("success").build();
            restResponse.setData(modifyPasswordResponse);

        } catch (AccountManagerException e) {
            ModifyPasswordResponse modifyPasswordResponse =
                    ModifyPasswordResponse.builder()
                            .errorCode(e.getErrorCode())
                            .message(e.getMessage())
                            .build();
            restResponse.setData(modifyPasswordResponse);
        } catch (Exception e) {
            logger.error("e: ", e);
            ModifyPasswordResponse modifyPasswordResponse =
                    ModifyPasswordResponse.builder()
                            .errorCode(ErrorCode.UndefinedError.getErrorCode())
                            .message(e.getMessage())
                            .build();
            restResponse.setData(modifyPasswordResponse);
        }

        return restResponse;
    }

    @RequestMapping(
            value = "/auth/routerLogin",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object routerLogin(@RequestBody String params) {
        RestResponse restResponse = RestResponse.newSuccess();
        try {
            LoginRequest loginRequest =
                    (LoginRequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/routerLogin", params, LoginRequest.class);

            if (logger.isDebugEnabled()) {
                logger.debug("routerLogin params: {}", loginRequest);
            }

            JwtManager jwtManager = serviceContext.getJwtManager();
            UAManager uaManager = serviceContext.getUaManager();
            String username = loginRequest.getUsername();

            UniversalAccount ua = null;
            try {
                ua = uaManager.getUA(username);
            } catch (AccountManagerException accountManagerException) {
                if (accountManagerException.getErrorCode()
                        == ErrorCode.UAAccountNotExist.getErrorCode()) {
                    logger.info("routerLogin username: {} not found and will create it", username);
                    String password = applicationConfig.getExt().getRouterLoginAccountPassword();
                    String confusedPassword = DigestUtils.sha256Hex(LoginSalt.LoginSalt + password);
                    ua = UniversalAccountBuilder.newUA(username, "", confusedPassword);
                    uaManager.setUA(ua);
                } else {
                    throw accountManagerException;
                }
            }

            JwtToken jwtToken = jwtManager.newToken(username);
            jwtManager.setTokenActive(jwtToken); // active it during login
            String tokenStr = jwtToken.getTokenStrWithPrefix(); // with prefix

            logger.info("routerLogin success: username:{} credential:{}", username, tokenStr);

            LoginResponse loginResponse =
                    LoginResponse.builder()
                            .errorCode(LoginResponse.SUCCESS)
                            .message("success")
                            .credential(tokenStr)
                            .universalAccount(uaManager.getUA(username).toInfo())
                            .build();

            restResponse.setData(loginResponse);

        } catch (Exception e) {
            logger.error("e", e);
            LoginResponse loginResponse =
                    LoginResponse.builder()
                            .errorCode(LoginResponse.ERROR)
                            .message(e.getMessage())
                            .credential(null)
                            .universalAccount(null)
                            .build();

            restResponse.setData(loginResponse);
        }

        return restResponse;
    }

    @RequestMapping(
            value = "/auth/register",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object register(@RequestBody String params) {

        RestResponse restResponse = RestResponse.newSuccess();
        try {
            RegisterRequest registerRequest =
                    (RegisterRequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/register", params, RegisterRequest.class);

            if (logger.isDebugEnabled()) {
                logger.debug("register request params: {}", registerRequest);
            }

            checkRegisterRequest(registerRequest);

            String username = registerRequest.getUsername().trim();
            String password = registerRequest.getPassword().trim();
            String randomToken = registerRequest.getRandomToken().trim();
            String authCode = registerRequest.getAuthCode();

            /** check if imageToken ok */
            AuthCodeManager authCodeManager = serviceContext.getAuthCodeManager();
            authCodeManager.authToken(randomToken, authCode);

            authCodeManager.authMailCode(username, registerRequest.getMailCode());

            UAManager uaManager = serviceContext.getUaManager();
            if (uaManager.isUAExist(username)) {
                throw new AccountManagerException(
                        ErrorCode.UAAccountExist.getErrorCode(),
                        "user '" + username + "' has already been registered");
            }

            String email = registerRequest.getEmail();
            if (authCodeManager.isNeedMailAuth() && uaManager.isEmailExist(email)) {
                throw new AccountManagerException(
                        ErrorCode.AccountExist.getErrorCode(),
                        "email address has been registered: " + email);
            }

            UniversalAccount newUA = UniversalAccountBuilder.newUA(username, email, password);

            uaManager.setUA(newUA);

            RegisterResponse registerResponse =
                    RegisterResponse.builder()
                            .errorCode(0)
                            .universalAccount(newUA.toInfo())
                            .message("success")
                            .build();

            restResponse.setData(registerResponse);

        } catch (AccountManagerException e) {
            RegisterResponse registerResponse =
                    RegisterResponse.builder()
                            .errorCode(e.getErrorCode())
                            .message(e.getMessage())
                            .build();
            restResponse.setData(registerResponse);
        } catch (Exception e) {
            logger.error("e: ", e);
            RegisterResponse registerResponse =
                    RegisterResponse.builder()
                            .errorCode(ErrorCode.UndefinedError.getErrorCode())
                            .message(e.getMessage())
                            .build();
            restResponse.setData(registerResponse);
        }
        return restResponse;
    }

    @RequestMapping(value = "/auth/pub", method = RequestMethod.POST)
    private Object getPubPostWay() {
        return getPub();
    }

    @RequestMapping(value = "/auth/pub", method = RequestMethod.GET)
    private Object getPub() {
        RestResponse restResponse = null;
        try {
            CryptoRSABase64Impl cryptoRSABase64Impl =
                    (CryptoRSABase64Impl)
                            serviceContext.getRestRequestFilter().getCryptoInterface();
            RSAKeyPairManager rsaKeyPairManager =
                    (RSAKeyPairManager) cryptoRSABase64Impl.getRsaKeyPairManager();
            String pub =
                    Base64.getEncoder()
                            .encodeToString(
                                    rsaKeyPairManager.getKeyPair().getPublic().getEncoded());

            PubResponse pubResponse =
                    PubResponse.builder().errorCode(0).pub(pub).message("success").build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(pubResponse);
            if (logger.isDebugEnabled()) {
                logger.debug("pub: {}", pubResponse.toString());
            }
        } catch (Exception e) {
            logger.error("e: ", e);
            restResponse = RestResponse.newFailed(e.getMessage());
        }

        return restResponse;
    }

    @RequestMapping(value = "/auth/authCode", method = RequestMethod.POST)
    private Object queryAuthCodePostWay() {
        return queryAuthCode();
    }

    @RequestMapping(value = "/auth/authCode", method = RequestMethod.GET)
    private Object queryAuthCode() {
        RestResponse restResponse;

        try {
            AuthCodeManager authCodeManager = serviceContext.getAuthCodeManager();
            AuthCode imageAuthCode = ImageCodeCreator.createAuthCode();
            authCodeManager.addAuthCode(imageAuthCode);

            AuthCodeResponse.AuthCodeInfo imageAuthCodeInfo = new AuthCodeResponse.AuthCodeInfo();
            imageAuthCodeInfo.setImageBase64(imageAuthCode.getImageBase64());
            imageAuthCodeInfo.setRandomToken(imageAuthCode.getToken());

            AuthCodeResponse imageAuthCodeResponse =
                    AuthCodeResponse.builder()
                            .errorCode(0)
                            .authCode(imageAuthCodeInfo)
                            .message("success")
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(imageAuthCodeResponse);

        } catch (Exception e) {
            logger.error("e: ", e);
            AuthCodeResponse imageAuthCodeResponse =
                    AuthCodeResponse.builder()
                            .errorCode(ErrorCode.UndefinedError.getErrorCode())
                            .message(e.getMessage())
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(imageAuthCodeResponse);
        }
        return restResponse;
    }

    @RequestMapping(value = "/auth/getUAVersion", produces = "application/json")
    private Object getUAVersion() {
        // if goes here, the user has login and token has not expired

        Long uaVersion = serviceContext.getUaManager().getCurrentLoginUA().getVersion();

        RestResponse restResponse = RestResponse.newSuccess();
        restResponse.setData(uaVersion);
        return restResponse;
    }

    @RequestMapping(value = "/auth/logout", produces = "application/json")
    private Object logout() {

        LogoutResponse logoutResponse;

        try {
            /* kick out all login token
                        UAManager uaManager = serviceContext.getUaManager();
                        UniversalAccount ua = uaManager.getCurrentLoginUA();

                        // reset token secret
                        ua.setTokenSec(UniversalAccountBuilder.newTokenStr());
                        uaManager.setUA(ua);
            */
            JwtManager jwtManager = serviceContext.getJwtManager();
            String tokenStr = jwtManager.getCurrentLoginToken().getTokenStr();
            jwtManager.setLogoutToken(tokenStr);
            logoutResponse =
                    LogoutResponse.builder()
                            .errorCode(LogoutResponse.SUCCESS)
                            .message("success")
                            .build();

        } catch (AccountManagerException e) {
            logoutResponse =
                    LogoutResponse.builder()
                            .errorCode(e.getErrorCode())
                            .message(e.getMessage())
                            .build();
        }
        RestResponse restResponse = RestResponse.newSuccess();
        restResponse.setData(logoutResponse);
        return restResponse;
    }

    private void checkAddChainAccountRequest(AddChainAccountRequest request)
            throws AccountManagerException {
        if (request.getType() == null) {
            throw new RequestParametersException("type has not given");
        }

        if (request.getPubKey() == null) {
            throw new RequestParametersException("pubKey has not given");
        }

        if (request.getSecKey() == null) {
            throw new RequestParametersException("secKey has not given");
        }

        if (request.getIsDefault() == null) {
            throw new RequestParametersException("isDefault has not given");
        }
    }

    @RequestMapping(
            value = "/auth/addChainAccount",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object addChainAccount(@RequestBody String params) {
        RestResponse restResponse;
        try {

            AddChainAccountRequest addChainAccountRequest =
                    (AddChainAccountRequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/addChainAccount",
                                            params,
                                            AddChainAccountRequest.class);

            checkAddChainAccountRequest(addChainAccountRequest);

            UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
            ChainAccount newChainAccount =
                    ChainAccountBuilder.buildFromRequest(addChainAccountRequest, ua.getUsername());
            ua.addChainAccount(newChainAccount);
            serviceContext.getUaManager().setUA(ua); // update to db

            AddChainAccountResponse addChainAccountResponse =
                    AddChainAccountResponse.builder().errorCode(0).message("success").build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(addChainAccountResponse);
        } catch (Exception e) {
            logger.error("e: ", e);
            AddChainAccountResponse addChainAccountResponse =
                    AddChainAccountResponse.builder().errorCode(1).message(e.getMessage()).build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(addChainAccountResponse);
        }
        return restResponse;
    }

    private void checkRemoveChainAccountRequest(RemoveChainAccountRequest request)
            throws RequestParametersException {
        if (request.getType() == null) {
            throw new RequestParametersException("type has not given");
        }

        if (request.getKeyID() == null) {
            throw new RequestParametersException("pubKey has not given");
        }
    }

    @RequestMapping(
            value = "/auth/removeChainAccount",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object removeChainAccount(@RequestBody String params) {
        RestResponse restResponse;

        try {

            RemoveChainAccountRequest removeChainAccountRequest =
                    (RemoveChainAccountRequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/removeChainAccount",
                                            params,
                                            RemoveChainAccountRequest.class);

            checkRemoveChainAccountRequest(removeChainAccountRequest);

            Integer keyID = removeChainAccountRequest.getKeyID();
            String type = removeChainAccountRequest.getType();

            UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
            ua.removeChainAccount(keyID, type);

            serviceContext.getUaManager().setUA(ua); // update to db

            RemoveChainAccountResponse removeChainAccountResponse =
                    RemoveChainAccountResponse.builder().errorCode(0).message("success").build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(removeChainAccountResponse);
        } catch (Exception e) {
            logger.error("e: ", e);
            RemoveChainAccountResponse removeChainAccountResponse =
                    RemoveChainAccountResponse.builder()
                            .errorCode(1)
                            .message(e.getMessage())
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(removeChainAccountResponse);
        }
        return restResponse;
    }

    private void checkSetDefaultAccountRequest(SetDefaultAccountRequest request)
            throws RequestParametersException {
        if (request.getType() == null) {
            throw new RequestParametersException("type has not given");
        }

        if (request.getKeyID() == null) {
            throw new RequestParametersException("pubKey has not given");
        }
    }

    @RequestMapping(
            value = "/auth/setDefaultAccount",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object setDefaultAccount(@RequestBody String params) {
        RestResponse restResponse;

        try {

            SetDefaultAccountRequest setDefaultAccountRequest =
                    (SetDefaultAccountRequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/setDefaultAccount",
                                            params,
                                            SetDefaultAccountRequest.class);

            checkSetDefaultAccountRequest(setDefaultAccountRequest);

            Integer keyID = setDefaultAccountRequest.getKeyID();
            String type = setDefaultAccountRequest.getType();

            UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
            ChainAccount chainAccount = ua.getChainAccountByKeyID(keyID);
            if (chainAccount == null) {
                throw new AccountManagerException(
                        ErrorCode.ChainAccountNotExist.getErrorCode(),
                        "keyID " + keyID.intValue() + " not found");
            }

            if (!chainAccount.getType().equals(type)) {
                throw new AccountManagerException(
                        ErrorCode.ChainAccountTypeNotFound.getErrorCode(),
                        "keyID " + keyID.intValue() + " of type " + type + " not found");
            }

            chainAccount.setDefault(true); // set
            ua.setChainAccount(chainAccount); // set to ua
            serviceContext.getUaManager().setUA(ua); // update to db

            SetDefaultAccountResponse setDefaultAccountResponse =
                    SetDefaultAccountResponse.builder().errorCode(0).message("success").build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(setDefaultAccountResponse);
        } catch (Exception e) {
            logger.error("e: ", e);
            SetDefaultAccountResponse setDefaultAccountResponse =
                    SetDefaultAccountResponse.builder()
                            .errorCode(1)
                            .message(e.getMessage())
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(setDefaultAccountResponse);
        }
        return restResponse;
    }

    @RequestMapping(value = "/auth/listAccount", produces = "application/json")
    private Object listAccount() {
        UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
        RestResponse response = RestResponse.newSuccess();
        response.setData(ua);

        return response;
    }

    @RequestMapping(value = "/auth/getUniversalAccount")
    private Object getAllAccounts() {
        UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
        RestResponse response = RestResponse.newSuccess();
        response.setData(ua.toDetails());

        return response;
    }

    static class UARequest {
        public String identity;
    }

    @RequestMapping(value = "/auth/getUniversalAccountByChainAccountIdentity")
    private Object getUniversalAccountByChainAccountIdentity(@RequestBody String params) {

        // must login with admin
        if (!serviceContext.getUaManager().isCurrentLoginAdminUA()) {
            return RestResponse.newFailed("Please use admin to query");
        }

        RestResponse restResponse;
        try {
            UARequest uARequest =
                    (UARequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/getUniversalAccountByChainAccountIdentity",
                                            params,
                                            UARequest.class);

            String identity = uARequest.identity;
            if (identity == null || identity.length() == 0) {
                throw new RequestParametersException("identity is not given");
            }

            UniversalAccount ua = serviceContext.getUaManager().getUAByChainAccount(identity);
            restResponse = RestResponse.newSuccess();
            restResponse.setData(ua.toDetails());
        } catch (Exception e) {
            logger.error("e: ", e);
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        return restResponse;
    }

    @RequestMapping(
            value = "/auth/admin/accessControlList",
            method = RequestMethod.GET,
            produces = "application/json")
    private Object getAccountNames(@RequestParam(required = false) String username) {
        RestResponse restResponse;

        try {
            UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
            // only admin can operate
            if (!ua.isAdmin()) {
                throw new Exception("Permission denied");
            }

            AccountAccessControlList[] data = null;
            if (username == null) {
                data = serviceContext.getUaManager().getAllAccessControlList(true);
            } else {
                data = new AccountAccessControlList[1];
                data[0] = serviceContext.getUaManager().getAccessControlListByName(username);
            }

            restResponse = RestResponse.newSuccess();
            restResponse.setData(data);

        } catch (Exception e) {
            logger.error("e: ", e);
            restResponse = RestResponse.newFailed(e.getMessage());
            restResponse.setData(null);
        }
        return restResponse;
    }

    @RequestMapping(
            value = "/auth/admin/accessControlList",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object getAccountNames(@RequestBody String params, @RequestParam String username) {
        RestResponse restResponse;

        try {
            if (username == null || username.length() == 0) {
                throw new Exception("username=xxx is not given");
            }

            UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
            // only admin can operate
            if (!ua.isAdmin()) {
                throw new Exception("Permission denied");
            }

            SetUniversalAccountACLRequest setUniversalAccountACLRequest =
                    (SetUniversalAccountACLRequest)
                            serviceContext
                                    .getRestRequestFilter()
                                    .fetchRequestObject(
                                            "/auth/setUniversalAccountAccessControlList",
                                            params,
                                            SetUniversalAccountACLRequest.class);

            String[] allowChainPaths = setUniversalAccountACLRequest.getAllowChainPaths();
            for (String allowChainPath : allowChainPaths) {
                Path path = Path.decode(allowChainPath);
                if (!path.isChainPath()) {
                    throw new AccountManagerException(
                            ErrorCode.InvalidPathFormat.getErrorCode(),
                            "Invalid chain path format(eg: payment.chain): " + allowChainPath);
                }
            }
            UniversalAccount userUA = serviceContext.getUaManager().getUA(username);

            userUA.setAllowChainPaths(allowChainPaths);
            serviceContext.getUaManager().setUA(userUA); // update to db

            SetUniversalAccountACLResponse setUniversalAccountACLResponse =
                    SetUniversalAccountACLResponse.builder()
                            .errorCode(0)
                            .message("success")
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(setUniversalAccountACLResponse);
        } catch (Exception e) {
            logger.error("e: ", e);
            SetUniversalAccountACLResponse setUniversalAccountACLResponse =
                    SetUniversalAccountACLResponse.builder()
                            .errorCode(1)
                            .message(e.getMessage())
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(setUniversalAccountACLResponse);
        }
        return restResponse;
    }

    @GetMapping("/auth/need-mail-auth")
    private RestResponse isNeedMailAuth() {
        RestResponse restResponse = RestResponse.newSuccess();
        restResponse.setData(serviceContext.getAuthCodeManager().isNeedMailAuth());
        return restResponse;
    }

    @PostMapping("/auth/mail-code")
    private RestResponse sendMailCode(@RequestBody String params) throws Exception {
        if (!serviceContext.getAuthCodeManager().isNeedMailAuth()) {
            return RestResponse.newFailed("Please turn on mail authentication");
        }

        MailCodeRequest mailCodeRequest =
                (MailCodeRequest)
                        serviceContext
                                .getRestRequestFilter()
                                .fetchRequestObject(
                                        "/auth/mail-code", params, MailCodeRequest.class);

        if (logger.isDebugEnabled()) {
            logger.debug("invitationCode request params: {}", mailCodeRequest);
        }

        String senderAddress = applicationConfig.getMail().getAddress();
        String senderPassword = applicationConfig.getMail().getPassword();
        String mailPort = applicationConfig.getMail().getSmtpPort();

        String toAddress = mailCodeRequest.getEmail();
        String username = mailCodeRequest.getUsername();
        if (Objects.isNull(toAddress) || toAddress.trim().equals("")) {

            toAddress = serviceContext.getUaManager().getEmail(username);
        }

        if (Objects.isNull(toAddress) || toAddress.trim().equals("")) {
            throw new AccountManagerException(
                    ErrorCode.MailNotFound.getErrorCode(), "mail address not found: " + username);
        }

        Properties props = System.getProperties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.port", mailPort);

        Session session =
                Session.getDefaultInstance(
                        props,
                        new Authenticator() {
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(senderAddress, senderPassword);
                            }
                        });

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(senderAddress));
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
        mimeMessage.setSubject("WeCross平台验证码");

        AuthCodeManager authCodeManager = serviceContext.getAuthCodeManager();
        MailCode mailCode = ImageCodeCreator.getMailCode();
        authCodeManager.addMailCode(username, mailCode);

        mimeMessage.setText(
                "尊敬的用户: \n    您的验证码为: "
                        + mailCode.getCode()
                        + "，"
                        + ImageCodeCreator.MAIL_CODE_VALIDITY_TINE / 60
                        + "分钟内有效。\n    本邮件由系统自动发送，请勿直接回复。");
        Transport.send(mimeMessage);

        return RestResponse.newSuccess();
    }
}
