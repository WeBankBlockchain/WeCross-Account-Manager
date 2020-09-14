package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.db.ChainAccountTableBean;
import com.webank.wecross.account.service.db.ChainAccountTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableBean;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.JPAException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UAManager {
    private static Logger logger = LoggerFactory.getLogger(UAManager.class);

    private UniversalAccountTableJPA universalAccountTableJPA;
    private ChainAccountTableJPA chainAccountTableJPA;

    private ThreadLocal<UniversalAccount> currentLoginUA = new ThreadLocal<>();

    public UAManager() {}

    public UniversalAccount getUA(String username) throws AccountManagerException {
        UniversalAccountTableBean universalAccountTableBean =
                universalAccountTableJPA.findByUsername(username);
        List<ChainAccountTableBean> chainAccountTableBeanList =
                chainAccountTableJPA.findByUsernameOrderByKeyIDDesc(username);
        if (universalAccountTableBean == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return UniversalAccountBuilder.build(universalAccountTableBean, chainAccountTableBeanList);
    }

    public boolean isUAExist(String username) {
        return Objects.nonNull(universalAccountTableJPA.findByUsername(username));
    }

    public void setUA(UniversalAccount ua) throws AccountManagerException {
        UniversalAccountTableBean universalAccountTableBean = ua.toTableBean();
        List<ChainAccount> chainAccounts = ua.getChainAccounts();
        List<ChainAccountTableBean> chainAccountTableBeanList = new LinkedList<>();

        for (ChainAccount chainAccount : chainAccounts) {
            ChainAccountTableBean chainAccountTableBean = chainAccount.toTableBean();
            chainAccountTableBeanList.add(chainAccountTableBean);
        }

        if (Objects.isNull(universalAccountTableJPA.saveAndFlush(universalAccountTableBean))) {
            throw new JPAException("set ua failed");
        }
        if (Objects.isNull(chainAccountTableJPA.saveAll(chainAccountTableBeanList))) {
            throw new JPAException("set chain account failed");
        }
        chainAccountTableJPA.flush();
    }

    // TODO: delete mock
    public void addMockUA() throws AccountManagerException {
        String username = "Tom";

        ChainAccount chainAccountBCOS = new BCOSChainAccount();
        chainAccountBCOS.setKeyID(0);
        chainAccountBCOS.setUsername(username);
        chainAccountBCOS.setDefault(true);
        chainAccountBCOS.setUAProof("uaproof");
        ((BCOSChainAccount) chainAccountBCOS).setPubKey("xxxpub");
        ((BCOSChainAccount) chainAccountBCOS).setSecKey("xxxsec");
        ((BCOSChainAccount) chainAccountBCOS).setAddress("xxxaddr");

        ChainAccount chainAccountBCOSGM = new BCOSGMChainAccount();
        chainAccountBCOSGM.setKeyID(1);
        chainAccountBCOSGM.setUsername(username);
        chainAccountBCOSGM.setDefault(true);
        chainAccountBCOSGM.setUAProof("uaproof");
        ((BCOSGMChainAccount) chainAccountBCOSGM).setPubKey("xxxpub");
        ((BCOSGMChainAccount) chainAccountBCOSGM).setSecKey("xxxsec");
        ((BCOSGMChainAccount) chainAccountBCOSGM).setAddress("xxxaddr");

        ChainAccount chainAccountFabric = new FabricChainAccount();
        chainAccountFabric.setKeyID(2);
        chainAccountFabric.setUsername(username);
        chainAccountFabric.setDefault(true);
        chainAccountFabric.setUAProof("uaproof");
        ((FabricChainAccount) chainAccountFabric).setCert("xxxcert");
        ((FabricChainAccount) chainAccountFabric).setKey("xxxkey");

        List<ChainAccount> chainAccounts = new LinkedList<>();
        chainAccounts.add(chainAccountBCOS);
        chainAccounts.add(chainAccountBCOSGM);
        chainAccounts.add(chainAccountFabric);

        UniversalAccount mockUA =
                UniversalAccount.builder()
                        .username(username)
                        .uaID("xxxxuaID")
                        .pubKey("xxxxxpub")
                        .password("123456")
                        .secKey("xxxsec")
                        .build();

        mockUA.setChainAccounts(chainAccounts);
        setUA(mockUA);
    }

    public void setUniversalAccountTableJPA(UniversalAccountTableJPA universalAccountTableJPA) {
        this.universalAccountTableJPA = universalAccountTableJPA;
    }

    public void setChainAccountTableJPA(ChainAccountTableJPA chainAccountTableJPA) {
        this.chainAccountTableJPA = chainAccountTableJPA;
    }

    public UniversalAccount getCurrentLoginUA() {
        return currentLoginUA.get();
    }

    public void setCurrentLoginUA(UniversalAccount currentLoginUA) {
        this.currentLoginUA.set(currentLoginUA);
    }

    public void setCurrentLoginUA(String username) throws AccountManagerException {
        UniversalAccount ua = getUA(username);
        this.setCurrentLoginUA(ua);
    }
}
