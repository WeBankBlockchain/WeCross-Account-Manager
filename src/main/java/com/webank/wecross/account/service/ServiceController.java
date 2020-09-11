package com.webank.wecross.account.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.service.account.ChainAccount;
import com.webank.wecross.account.service.account.ChainAccountBuilder;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.account.UniversalAccount;
import com.webank.wecross.account.service.account.UniversalAccountFactory;
import com.webank.wecross.account.service.authentication.JwtManager;

import javax.annotation.Resource;

import com.webank.wecross.account.service.authentication.packet.AddChainAccountRequest;
import com.webank.wecross.account.service.authentication.packet.AddChainAccountResponse;
import com.webank.wecross.account.service.authentication.packet.LogoutResponse;
import com.webank.wecross.account.service.authentication.packet.RegisterRequest;
import com.webank.wecross.account.service.authentication.packet.RegisterResponse;
import com.webank.wecross.account.service.authentication.packet.SetDefaultAccountRequest;
import com.webank.wecross.account.service.authentication.packet.SetDefaultAccountResponse;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.AddChainAccountException;
import com.webank.wecross.account.service.exception.RegisterException;
import com.webank.wecross.account.service.exception.SetChainAccountException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class ServiceController {
    private ObjectMapper objectMapper = new ObjectMapper();

    public ServiceController() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Resource
    ServiceContext serviceContext;

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
            throw new RegisterException("user '" + request.getUsername() + "' has already been registered");
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
                    objectMapper.readValue(params, new TypeReference<RestRequest<RegisterRequest>>() {
                    });
        } catch (Exception e) {
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        try {

            RegisterRequest registerRequest = restRequest.getData();

            checkRegisterRequest(registerRequest);

            String username = registerRequest.getUsername();
            String password = registerRequest.getPassword();

            UAManager uaManager = serviceContext.getUaManager();
            UniversalAccount newUA = UniversalAccountFactory.newUA(username, password);

            uaManager.setUA(newUA);

            RegisterResponse registerResponse = RegisterResponse.builder().errorCode(0).message("success").build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(registerResponse);

        } catch (Exception e) {
            RegisterResponse registerResponse = RegisterResponse.builder().errorCode(1).message(e.getMessage()).build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(registerResponse);
        }
        return restResponse;
    }

    @RequestMapping(value = "/auth/logout", produces = "application/json")
    private Object logout(@RequestBody String params) {

        LogoutResponse logoutResponse;

        try {

            JwtManager jwtManager = serviceContext.getJwtManager();
            String tokenStr = jwtManager.getCurrentLoginToken().getTokenStr();
            jwtManager.setLogoutToken(tokenStr);

            logoutResponse = LogoutResponse.builder().errorCode(LogoutResponse.SUCCESS).message("success").build();

        } catch (AccountManagerException e) {
            logoutResponse = LogoutResponse.builder().errorCode(LogoutResponse.ERROR).message(e.getMessage()).build();
        }
        RestResponse restResponse = RestResponse.newSuccess();
        restResponse.setData(logoutResponse);
        return restResponse;
    }

    private void checkAddChainAccountRequest(AddChainAccountRequest request) throws AccountManagerException {
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
                    objectMapper.readValue(params, new TypeReference<RestRequest<AddChainAccountRequest>>() {
                    });
        } catch (Exception e) {
            restResponse = RestResponse.newFailed(e.getMessage());
            return restResponse;
        }

        try {

            AddChainAccountRequest addChainAccountRequest = restRequest.getData();
            checkAddChainAccountRequest(addChainAccountRequest);

            UniversalAccount ua = serviceContext.getUaManager().getCurrentLoginUA();
            ChainAccount newChainAccount = ChainAccountBuilder.buildFromRequest(addChainAccountRequest, ua.getUsername());
            ua.addChainAccount(newChainAccount);
            serviceContext.getUaManager().setUA(ua); // update to db

            AddChainAccountResponse addChainAccountResponse = AddChainAccountResponse.builder().errorCode(0).message("success").build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(addChainAccountResponse);
        } catch (Exception e) {
            AddChainAccountResponse addChainAccountResponse = AddChainAccountResponse.builder().errorCode(1).message(e.getMessage()).build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(addChainAccountResponse);
        }
        return restResponse;
    }

    private void checkSetDefaultAccountRequest(SetDefaultAccountRequest request) throws SetChainAccountException {
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
                    objectMapper.readValue(params, new TypeReference<RestRequest<SetDefaultAccountRequest>>() {
                    });
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
                throw new SetChainAccountException("keyID " + keyID.intValue() + " of type " + type + " not found");
            }

            chainAccount.setDefault(true); // set
            ua.setChainAccount(chainAccount); // set to ua
            serviceContext.getUaManager().setUA(ua); // update to db

            SetDefaultAccountResponse setDefaultAccountResponse = SetDefaultAccountResponse.builder().errorCode(0).message("success").build();
            restResponse = RestResponse.newSuccess();
            restResponse.setData(setDefaultAccountResponse);
        } catch (Exception e) {
            SetDefaultAccountResponse setDefaultAccountResponse = SetDefaultAccountResponse.builder().errorCode(1).message(e.getMessage()).build();
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
}
