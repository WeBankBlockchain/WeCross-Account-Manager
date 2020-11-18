package com.webank.wecross.account.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.service.account.ChainAccount;
import com.webank.wecross.account.service.account.ChainAccountBuilder;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.account.UniversalAccount;
import com.webank.wecross.account.service.account.UniversalAccountBuilder;
import com.webank.wecross.account.service.authentication.JwtManager;
import com.webank.wecross.account.service.authentication.packet.AddChainAccountRequest;
import com.webank.wecross.account.service.authentication.packet.AddChainAccountResponse;
import com.webank.wecross.account.service.authentication.packet.ImageAuthCodeResponse;
import com.webank.wecross.account.service.authentication.packet.LogoutResponse;
import com.webank.wecross.account.service.authentication.packet.RegisterRequest;
import com.webank.wecross.account.service.authentication.packet.RegisterResponse;
import com.webank.wecross.account.service.authentication.packet.SetDefaultAccountRequest;
import com.webank.wecross.account.service.authentication.packet.SetDefaultAccountResponse;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.AddChainAccountException;
import com.webank.wecross.account.service.exception.RegisterException;
import com.webank.wecross.account.service.exception.SetChainAccountException;
import com.webank.wecross.account.service.image.authcode.ImageAuthCode;
import com.webank.wecross.account.service.image.authcode.ImageAuthCodeCreator;
import com.webank.wecross.account.service.image.authcode.ImageAuthCodeManager;
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

    private void checkRegisterRequest(RegisterRequest request) throws AccountManagerException {
        if (request.getUsername() == null) {
            throw new RegisterException("username has not given");
        }

        if (request.getPassword() == null) {
            throw new RegisterException("password has not given");
        }

        if (request.getUsername().length() > 256) {
            throw new RegisterException("username is too long, limit 256");
        }

        if (request.getPassword().length() > 256) {
            throw new RegisterException("password is too long, limit 256");
        }

        UAManager uaManager = serviceContext.getUaManager();
        if (uaManager.isUAExist(request.getUsername())) {
            throw new RegisterException(
                    "user '" + request.getUsername() + "' has already been registered");
        }
    }

    @RequestMapping(
            value = "/auth/register",
            method = RequestMethod.POST,
            produces = "application/json")
    private Object register(@RequestBody String params) {
        RestRequest<RegisterRequest> restRequest;
        RestResponse restResponse;
        try {
            restRequest =
                    objectMapper.readValue(
                            params, new TypeReference<RestRequest<RegisterRequest>>() {});
        } catch (Exception e) {
            logger.error("e: ", e);
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        try {

            RegisterRequest registerRequest = restRequest.getData();

            checkRegisterRequest(registerRequest);

            String username = registerRequest.getUsername();
            String password = registerRequest.getPassword();
            String imageCode = registerRequest.getImageAuthCode();
            String imageToken = registerRequest.getImageToken();

            if (imageToken == null) {
                logger.error(
                        " invalid request, imageToken field not exist, request: {}",
                        registerRequest);
                throw new RuntimeException("invalid request, \"imageToken\" field not exist");
            }

            /** check if imageToken ok */
            ImageAuthCodeManager imageAuthCodeManager = serviceContext.getImageAuthCodeManager();
            ImageAuthCode imageAuthCode = imageAuthCodeManager.get(imageToken);
            if (imageAuthCode == null) {
                logger.error(
                        "image auth token not exist, code: {}, token:{}", imageCode, imageToken);
                throw new RuntimeException("image auth token not exist, token: " + imageToken);
            }

            if (imageAuthCode.isExpired()) {
                logger.error("image auth token expired, token:{}", imageAuthCode);
                imageAuthCodeManager.remove(imageToken);
                throw new RuntimeException("image auth token expired, token: " + imageToken);
            }

            if (!imageAuthCode.getCode().equalsIgnoreCase(imageCode)) {
                logger.error("image auth code not match, request: {}", imageAuthCode);
                throw new RuntimeException(
                        "image auth code not match, request: "
                                + imageCode
                                + ", expect: "
                                + imageAuthCode.getCode());
            }

            imageAuthCodeManager.remove(imageToken);

            UAManager uaManager = serviceContext.getUaManager();
            UniversalAccount newUA = UniversalAccountBuilder.newUA(username, password);

            uaManager.setUA(newUA);

            RegisterResponse registerResponse =
                    RegisterResponse.builder()
                            .errorCode(0)
                            .universalAccount(newUA.toInfo())
                            .message("success")
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(registerResponse);

        } catch (Exception e) {
            logger.error("e: ", e);
            RegisterResponse registerResponse =
                    RegisterResponse.builder().errorCode(1).message(e.getMessage()).build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(registerResponse);
        }
        return restResponse;
    }

    @RequestMapping(value = "/auth/imageAuthCode", method = RequestMethod.GET)
    private Object imageAuthCode() {
        RestResponse restResponse;

        try {

            ImageAuthCodeManager imageAuthCodeManager = serviceContext.getImageAuthCodeManager();
            ImageAuthCode imageAuthCode = ImageAuthCodeCreator.createImageAuthCode();
            imageAuthCodeManager.add(imageAuthCode);

            ImageAuthCodeResponse.ImageAuthCodeInfo imageAuthCodeInfo =
                    new ImageAuthCodeResponse.ImageAuthCodeInfo();
            imageAuthCodeInfo.setImageBase64(imageAuthCode.getImageBase64());
            imageAuthCodeInfo.setImageToken(imageAuthCode.getToken());

            ImageAuthCodeResponse imageAuthCodeResponse =
                    ImageAuthCodeResponse.builder()
                            .errorCode(0)
                            .imageAuthCodeInfo(imageAuthCodeInfo)
                            .message("success")
                            .build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(imageAuthCodeResponse);

        } catch (Exception e) {
            logger.error("e: ", e);
            ImageAuthCodeResponse imageAuthCodeResponse =
                    ImageAuthCodeResponse.builder().errorCode(1).message(e.getMessage()).build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(imageAuthCodeResponse);
        }
        return restResponse;
    }

    @RequestMapping(value = "/auth/hasLogin", produces = "application/json")
    private Object hasLogin(@RequestBody String params) {
        // if goes here, the user has login and token has not expired
        RestResponse restResponse = RestResponse.newSuccess();
        restResponse.setData(null);
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
                            .errorCode(LogoutResponse.ERROR)
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
            throw new AddChainAccountException("type has not given");
        }

        if (request.getPubKey() == null) {
            throw new AddChainAccountException("pubKey has not given");
        }

        if (request.getSecKey() == null) {
            throw new AddChainAccountException("secKey has not given");
        }

        if (request.getIsDefault() == null) {
            throw new AddChainAccountException("isDefault has not given");
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

    private void checkSetDefaultAccountRequest(SetDefaultAccountRequest request)
            throws SetChainAccountException {
        if (request.getType() == null) {
            throw new SetChainAccountException("type has not given");
        }

        if (request.getKeyID() == null) {
            throw new SetChainAccountException("pubKey has not given");
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
                throw new SetChainAccountException("keyID " + keyID.intValue() + " not found");
            }

            if (!chainAccount.getType().equals(type)) {
                throw new SetChainAccountException(
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
                throw new AccountManagerException("identity is not given");
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
