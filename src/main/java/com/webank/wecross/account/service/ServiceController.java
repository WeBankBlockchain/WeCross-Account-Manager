package com.webank.wecross.account.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.service.account.ChainAccount;
import com.webank.wecross.account.service.account.ChainAccountBuilder;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.account.UniversalAccount;
import com.webank.wecross.account.service.account.UniversalAccountBuilder;
import com.webank.wecross.account.service.authcode.AuthCode;
import com.webank.wecross.account.service.authcode.AuthCodeManager;
import com.webank.wecross.account.service.authcode.ImageCodeCreator;
import com.webank.wecross.account.service.authentication.JwtManager;
import com.webank.wecross.account.service.authentication.packet.AddChainAccountRequest;
import com.webank.wecross.account.service.authentication.packet.AddChainAccountResponse;
import com.webank.wecross.account.service.authentication.packet.AuthCodeResponse;
import com.webank.wecross.account.service.authentication.packet.LogoutResponse;
import com.webank.wecross.account.service.authentication.packet.ModifyPasswordRequest;
import com.webank.wecross.account.service.authentication.packet.ModifyPasswordResponse;
import com.webank.wecross.account.service.authentication.packet.RegisterRequest;
import com.webank.wecross.account.service.authentication.packet.RegisterResponse;
import com.webank.wecross.account.service.authentication.packet.RemoveChainAccountRequest;
import com.webank.wecross.account.service.authentication.packet.RemoveChainAccountResponse;
import com.webank.wecross.account.service.authentication.packet.SetDefaultAccountRequest;
import com.webank.wecross.account.service.authentication.packet.SetDefaultAccountResponse;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ErrorCode;
import com.webank.wecross.account.service.exception.RequestParametersException;
import com.webank.wecross.account.service.utils.CommonUtility;
import java.util.UUID;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {
    private static Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    public ServiceController() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Resource ServiceContext serviceContext;

    @RequestMapping("/test")
    private Object test() {
        return "OK!";
    }

    /*
    @RequestMapping(value = "/**", method = RequestMethod.POST, produces = "application/json")
    private Object echo(@RequestBody String params) {
        return params;
    }
    */

    private void checkModifyPasswordRequest(ModifyPasswordRequest request)
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
    }

    private void checkRegisterRequest(RegisterRequest request) throws AccountManagerException {
        if (request.getUsername() == null) {
            throw new RequestParametersException("username has not given");
        }

        if (request.getPassword() == null) {
            throw new RequestParametersException("password has not given");
        }

        if (request.getImageToken() == null) {
            throw new RequestParametersException("image auth token has not given");
        }

        if (request.getUsername().length() > 256) {
            throw new RequestParametersException("username is too long, limit 256");
        }

        if (request.getPassword().length() > 256) {
            throw new RequestParametersException("password is too long, limit 256");
        }

        UAManager uaManager = serviceContext.getUaManager();
        if (uaManager.isUAExist(request.getUsername())) {
            throw new AccountManagerException(
                    ErrorCode.UAAccountExist.getErrorCode(),
                    "user '" + request.getUsername() + "' has already been registered");
        }
    }

    @RequestMapping(
            value = "/auth/modifyPassword",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object modifyPassword(@RequestBody String params) {
        RestResponse restResponse = RestResponse.newSuccess();
        try {

            RestRequest<ModifyPasswordRequest> restRequest =
                    objectMapper.readValue(
                            params, new TypeReference<RestRequest<ModifyPasswordRequest>>() {});

            ModifyPasswordRequest modifyPasswordRequest = restRequest.getData();

            logger.info("ModifyPasswordRequest: {}", modifyPasswordRequest);
            checkModifyPasswordRequest(modifyPasswordRequest);

            UniversalAccount ua =
                    serviceContext.getUaManager().getUA(modifyPasswordRequest.getUsername());
            String passwordWithSalt = CommonUtility.mixPassWithSalt(ua.getPassword(), ua.getSalt());
            if (!passwordWithSalt.equals(ua.getPassword())) {
                throw new RuntimeException("password incorrect.");
            }

            ua.setPassword(
                    CommonUtility.mixPassWithSalt(
                            modifyPasswordRequest.getNewPassword(), UUID.randomUUID().toString()));

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
            value = "/auth/register",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object register(@RequestBody String params) {

        RestResponse restResponse = RestResponse.newSuccess();
        try {

            RestRequest<RegisterRequest> restRequest =
                    objectMapper.readValue(
                            params, new TypeReference<RestRequest<RegisterRequest>>() {});

            RegisterRequest registerRequest = restRequest.getData();

            checkRegisterRequest(registerRequest);

            String username = registerRequest.getUsername();
            String password = registerRequest.getPassword();
            String imageCode = registerRequest.getAuthCode();
            String imageToken = registerRequest.getImageToken();

            /** check if imageToken ok */
            AuthCodeManager authCodeManager = serviceContext.getAuthCodeManager();
            AuthCode imageAuthCode = authCodeManager.get(imageToken);
            if (imageAuthCode == null) {
                logger.error(
                        "image auth token not exist, code: {}, token:{}", imageCode, imageToken);
                throw new AccountManagerException(
                        ErrorCode.ImageAuthTokenNotExist.getErrorCode(),
                        "image auth token not found");
            }

            if (imageAuthCode.isExpired()) {
                logger.error("image auth token expired, token:{}", imageAuthCode);
                authCodeManager.remove(imageToken);
                throw new AccountManagerException(
                        ErrorCode.ImageAuthTokenExpired.getErrorCode(), "image auth token expired");
            }

            if (!imageAuthCode.getCode().equalsIgnoreCase(imageCode)) {
                logger.error("image auth code not match, request: {}", imageAuthCode);
                throw new AccountManagerException(
                        ErrorCode.ImageAuthTokenNotMatch.getErrorCode(),
                        "image auth code does not match");
            }

            authCodeManager.remove(imageToken);

            UAManager uaManager = serviceContext.getUaManager();
            UniversalAccount newUA = UniversalAccountBuilder.newUA(username, password);

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

    @RequestMapping(value = "/auth/authCode", method = RequestMethod.GET)
    private Object getAuthCode() {
        RestResponse restResponse;

        try {
            AuthCodeManager authCodeManager = serviceContext.getAuthCodeManager();
            AuthCode imageAuthCode = ImageCodeCreator.createAuthCode();
            authCodeManager.add(imageAuthCode);

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
    private Object getUAVersion(@RequestBody String params) {
        // if goes here, the user has login and token has not expired

        Long uaVersion = serviceContext.getUaManager().getCurrentLoginUA().getVersion();

        RestResponse restResponse = RestResponse.newSuccess();
        restResponse.setData(uaVersion);
        return restResponse;
    }

    @RequestMapping(value = "/auth/logout", produces = "application/json")
    private Object logout(@RequestBody String params) {

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
        RestRequest<AddChainAccountRequest> restRequest;
        RestResponse restResponse;
        try {
            restRequest =
                    objectMapper.readValue(
                            params, new TypeReference<RestRequest<AddChainAccountRequest>>() {});
        } catch (Exception e) {
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        try {

            AddChainAccountRequest addChainAccountRequest = restRequest.getData();
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
        RestRequest<RemoveChainAccountRequest> restRequest;
        RestResponse restResponse;
        try {
            restRequest =
                    objectMapper.readValue(
                            params, new TypeReference<RestRequest<RemoveChainAccountRequest>>() {});
        } catch (Exception e) {
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        try {

            RemoveChainAccountRequest removeChainAccountRequest = restRequest.getData();
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
        RestRequest<SetDefaultAccountRequest> restRequest;
        RestResponse restResponse;
        try {
            restRequest =
                    objectMapper.readValue(
                            params, new TypeReference<RestRequest<SetDefaultAccountRequest>>() {});
        } catch (Exception e) {
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        try {

            SetDefaultAccountRequest setDefaultAccountRequest = restRequest.getData();
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
    private Object listAccount(@RequestBody String params) {
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

        RestRequest<UARequest> restRequest;
        RestResponse restResponse;
        try {
            restRequest =
                    objectMapper.readValue(params, new TypeReference<RestRequest<UARequest>>() {});

            String identity = restRequest.getData().identity;
            if (identity == null || identity.length() == 0) {
                throw new RequestParametersException("identity is not given");
            }

            UniversalAccount ua = serviceContext.getUaManager().getUAByChainAccount(identity);
            restResponse = RestResponse.newSuccess();
            restResponse.setData(ua.toDetails());
        } catch (Exception e) {
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        return restResponse;
    }
}
