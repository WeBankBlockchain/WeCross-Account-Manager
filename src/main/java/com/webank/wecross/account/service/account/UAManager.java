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
    private String adminName;

    private ThreadLocal<UniversalAccount> currentLoginUA = new ThreadLocal<>();

    public UAManager() {}

    public boolean isUAExist(String username) {
        return Objects.nonNull(universalAccountTableJPA.findByUsername(username));
    }

    public UniversalAccount getUA(String username) throws AccountManagerException {
        UniversalAccountTableBean universalAccountTableBean =
                universalAccountTableJPA.findByUsername(username);
        List<ChainAccountTableBean> chainAccountTableBeanList =
                chainAccountTableJPA.findByUsernameOrderByKeyIDDesc(username);
        if (universalAccountTableBean == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UniversalAccount ua =
                UniversalAccountBuilder.build(universalAccountTableBean, chainAccountTableBeanList);
        ua.setAdmin(username.equals(adminName));
        return ua;
    }

    public void setUA(UniversalAccount ua) throws AccountManagerException {
        UniversalAccountTableBean universalAccountTableBean = ua.toTableBean();
        List<ChainAccount> chainAccounts = ua.getChainAccounts();
        List<ChainAccountTableBean> chainAccountTableBeanList = new LinkedList<>();

        for (ChainAccount chainAccount : chainAccounts) {
            ChainAccountTableBean chainAccountTableBean = chainAccount.toTableBean();
            chainAccountTableBeanList.add(chainAccountTableBean);
        }

        try {
            universalAccountTableJPA.saveAndFlush(universalAccountTableBean);
        } catch (Exception e) {
            throw new JPAException("set ua failed: " + e.getMessage());
        }
        try {

            chainAccountTableJPA.saveAll(chainAccountTableBeanList);

        } catch (Exception e) {
            throw new JPAException(
                    "set chain account failed (chain account existing?): " + e.getMessage());
        }
        chainAccountTableJPA.flush();
    }

    public UniversalAccount getUAByChainAccount(String chainAccountIdentity)
            throws AccountManagerException {
        List<ChainAccountTableBean> chainAccountTableBeanList =
                chainAccountTableJPA.findByIdentityOrderByKeyIDDesc(chainAccountIdentity);
        if (chainAccountTableBeanList == null) {
            throw new UsernameNotFoundException("Chain account not found: " + chainAccountIdentity);
        }

        // check username are the same
        String username = chainAccountTableBeanList.get(0).getUsername();
        for (ChainAccountTableBean chainAccountTableBean : chainAccountTableBeanList) {
            if (!chainAccountTableBean.getUsername().equals(username)) {
                logger.warn(
                        "Found 2 username:[{},{}] under a chain account identity: {}",
                        username,
                        chainAccountTableBean.getUsername(),
                        chainAccountIdentity);
            }
        }

        return getUA(username);
    }

    // TODO: delete mock
    public void addMockUA() throws AccountManagerException {
        String username = "Tom";

        ChainAccount chainAccountBCOS = new BCOSChainAccount();
        chainAccountBCOS.setKeyID(0);
        chainAccountBCOS.setUsername(username);
        chainAccountBCOS.setDefault(true);
        ((BCOSChainAccount) chainAccountBCOS).setPubKey("xxxpub");
        ((BCOSChainAccount) chainAccountBCOS).setSecKey("xxxsec");
        ((BCOSChainAccount) chainAccountBCOS).setAddress("xxxaddr");

        ChainAccount chainAccountBCOSGM = new BCOSGMChainAccount();
        chainAccountBCOSGM.setKeyID(1);
        chainAccountBCOSGM.setUsername(username);
        chainAccountBCOSGM.setDefault(true);
        ((BCOSGMChainAccount) chainAccountBCOSGM).setPubKey("xxxpub");
        ((BCOSGMChainAccount) chainAccountBCOSGM).setSecKey("xxxsec");
        ((BCOSGMChainAccount) chainAccountBCOSGM).setAddress("xxxaddrgm");

        ChainAccount chainAccountFabric = new FabricChainAccount();
        chainAccountFabric.setKeyID(2);
        chainAccountFabric.setUsername(username);
        chainAccountFabric.setDefault(true);
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

    public void initAdminUA(String username, String password) throws AccountManagerException {
        setAdminName(username);
        UniversalAccount admin;
        try {
            admin = getUA(username);

            logger.info("Found adminUA. Check: {}", username);
            if (!admin.getUsername().equals(username)) {
                throw new AccountManagerException("Invalid adminUA usernmame: " + username);
            }

            if (!admin.getPassword().equals(password)) {
                System.out.println("Invalid adminUA password, please check.");
                throw new AccountManagerException("Invalid adminUA password, please check.");
            }

        } catch (UsernameNotFoundException e) {
            // not found
            logger.info("AdminUA not found. Generate: {}", username);
            admin = UniversalAccountBuilder.newUA(username, password);
            setUA(admin);
            logger.info("AdminUA generate success!");
        }
    }

    public boolean isAdminUA(UniversalAccount universalAccount) {
        return universalAccount.getUsername().equals(adminName);
    }

    public boolean isCurrentLoginAdminUA() {
        return isAdminUA(getCurrentLoginUA());
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

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
}
