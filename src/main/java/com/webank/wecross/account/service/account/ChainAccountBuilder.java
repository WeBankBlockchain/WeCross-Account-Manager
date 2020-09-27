package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.authentication.packet.AddChainAccountRequest;
import com.webank.wecross.account.service.config.Default;
import com.webank.wecross.account.service.db.ChainAccountTableBean;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.AddChainAccountException;
import com.webank.wecross.account.service.exception.UnknownChainAccountTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainAccountBuilder {
    private static Logger logger = LoggerFactory.getLogger(ChainAccountBuilder.class);

    public static ChainAccount buildFromTableBean(ChainAccountTableBean tableBean)
            throws AccountManagerException {
        String type = tableBean.getType();
        switch (type) {
            case Default.BCOS_STUB_TYPE:
                return buildBCOS(tableBean);
            case Default.BCOS_GM_STUB_TYPE:
                return buildBCOSGM(tableBean);
            case Default.FABRIC_STUB_TYPE:
                return buildFabric(tableBean);
            default:
                logger.error("table bean unkown ChainAccount type: " + type);
                throw new UnknownChainAccountTypeException("Unkown ChainAccount type: " + type);
        }
    }

    private static BCOSChainAccount buildBCOS(ChainAccountTableBean tableBean) {
        BCOSChainAccount account = new BCOSChainAccount();
        account.setId(tableBean.getId());
        account.setKeyID(tableBean.getKeyID());
        account.setIdentity(tableBean.getIdentity());
        account.setUsername(tableBean.getUsername());
        account.setDefault(tableBean.isDefault());

        account.setPubKey(tableBean.getPub());
        account.setSecKey(tableBean.getSec());
        account.setAddress(tableBean.getExt0());
        return account;
    }

    private static BCOSGMChainAccount buildBCOSGM(ChainAccountTableBean tableBean) {
        BCOSGMChainAccount account = new BCOSGMChainAccount();
        account.setId(tableBean.getId());
        account.setKeyID(tableBean.getKeyID());
        account.setIdentity(tableBean.getIdentity());
        account.setUsername(tableBean.getUsername());
        account.setDefault(tableBean.isDefault());

        account.setPubKey(tableBean.getPub());
        account.setSecKey(tableBean.getSec());
        account.setAddress(tableBean.getExt0());
        return account;
    }

    private static FabricChainAccount buildFabric(ChainAccountTableBean tableBean) {
        FabricChainAccount account = new FabricChainAccount();
        account.setId(tableBean.getId());
        account.setKeyID(tableBean.getKeyID());
        account.setIdentity(tableBean.getIdentity());
        account.setUsername(tableBean.getUsername());
        account.setDefault(tableBean.isDefault());

        account.setCert(tableBean.getPub());
        account.setKey(tableBean.getSec());
        account.setMspID(tableBean.getExt0());
        return account;
    }

    public static ChainAccount buildFromRequest(AddChainAccountRequest request, String username)
            throws AddChainAccountException {
        String type = request.getType();
        switch (type) {
            case Default.BCOS_STUB_TYPE:
                return buildBCOS(request, username);
            case Default.BCOS_GM_STUB_TYPE:
                return buildBCOSGM(request, username);
            case Default.FABRIC_STUB_TYPE:
                return buildFabric(request, username);
            default:
                logger.error("request unkown ChainAccount type: " + type);
                throw new AddChainAccountException("Unkown ChainAccount type: " + type);
        }
    }

    private static ChainAccount buildBCOS(AddChainAccountRequest request, String username)
            throws AddChainAccountException {
        BCOSChainAccount account = new BCOSChainAccount();
        account.setUsername(username);
        account.setDefault(request.getIsDefault().booleanValue());
        account.setPubKey(request.getPubKey());
        account.setIdentity(request.getExt());
        account.setSecKey(request.getSecKey());
        account.setAddress(request.getExt());
        return account;
    }

    private static ChainAccount buildBCOSGM(AddChainAccountRequest request, String username)
            throws AddChainAccountException {
        BCOSGMChainAccount account = new BCOSGMChainAccount();
        account.setUsername(username);
        account.setDefault(request.getIsDefault().booleanValue());
        account.setPubKey(request.getPubKey());
        account.setIdentity(request.getExt());
        account.setSecKey(request.getSecKey());
        account.setAddress(request.getExt());
        return account;
    }

    private static ChainAccount buildFabric(AddChainAccountRequest request, String username)
            throws AddChainAccountException {
        FabricChainAccount account = new FabricChainAccount();
        account.setUsername(username);
        account.setDefault(request.getIsDefault().booleanValue());
        account.setIdentity(request.getPubKey());
        account.setCert(request.getPubKey());
        account.setKey(request.getSecKey());
        account.setMspID(request.getExt());
        return account;
    }
}
