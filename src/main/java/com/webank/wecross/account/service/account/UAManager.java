package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.db.ChainAccountTableBean;
import com.webank.wecross.account.service.db.ChainAccountTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountACLTableBean;
import com.webank.wecross.account.service.db.UniversalAccountACLTableJPA;
import com.webank.wecross.account.service.db.UniversalAccountTableBean;
import com.webank.wecross.account.service.db.UniversalAccountTableJPA;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ErrorCode;
import com.webank.wecross.account.service.exception.JPAException;
import com.webank.wecross.account.service.exception.UndefinedErrorException;
import com.webank.wecross.account.service.utils.PassWordUtility;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UAManager {
    private static Logger logger = LoggerFactory.getLogger(UAManager.class);

    private UniversalAccountTableJPA universalAccountTableJPA;
    private ChainAccountTableJPA chainAccountTableJPA;
    private UniversalAccountACLTableJPA universalAccountACLTableJPA;
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
        UniversalAccountACLTableBean universalAccountACLTableBean =
                universalAccountACLTableJPA.findByUsername(username);

        if (universalAccountTableBean == null) {
            throw new AccountManagerException(
                    ErrorCode.UAAccountNotExist.getErrorCode(), "User not found: " + username);
        }

        UniversalAccount ua =
                UniversalAccountBuilder.build(
                        universalAccountTableBean,
                        chainAccountTableBeanList,
                        universalAccountACLTableBean);
        ua.setAdmin(username.equals(adminName));
        return ua;
    }

    public void setUA(UniversalAccount ua) throws AccountManagerException {
        UniversalAccountTableBean universalAccountTableBean = ua.toTableBean();
        List<ChainAccount> chainAccounts = ua.getChainAccounts();
        List<ChainAccountTableBean> chainAccountTableBeanList = new LinkedList<>();
        UniversalAccountACLTableBean universalAccountACLTableBean = ua.toACLBean();

        for (ChainAccount chainAccount : chainAccounts) {
            ChainAccountTableBean chainAccountTableBean = chainAccount.toTableBean();
            chainAccountTableBeanList.add(chainAccountTableBean);
        }

        try {
            universalAccountTableBean.setUpdateTimestamp(System.currentTimeMillis());
            universalAccountTableJPA.saveAndFlush(universalAccountTableBean);
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("e: ", e);
            throw new AccountManagerException(
                    ErrorCode.UAHasBeenModified.getErrorCode(),
                    "The "
                            + ua.getUsername()
                            + "'data is being modified simultaneously, please try again.");
        } catch (Exception e) {
            logger.error("set ua failed, e: ", e);
            throw new JPAException("set ua failed: " + e.getMessage());
        }

        try {

            chainAccountTableJPA.saveAll(chainAccountTableBeanList);

        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("e: ", e);
            throw new AccountManagerException(
                    ErrorCode.UAHasBeenModified.getErrorCode(),
                    "The "
                            + ua.getUsername()
                            + "'data is being modified simultaneously, please try again.");
        } catch (Exception e) {
            logger.error("Set chain account failed, chain account is owned by other UA? ", e);
            throw new JPAException("Chain account is owned by other UA");
        }

        try {
            universalAccountACLTableBean.setUpdateTimestamp(System.currentTimeMillis());
            universalAccountACLTableJPA.saveAndFlush(universalAccountACLTableBean);
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("e: ", e);
            throw new AccountManagerException(
                    ErrorCode.UAHasBeenModified.getErrorCode(),
                    "The "
                            + ua.getUsername()
                            + "'data is being modified simultaneously, please try again.");
        } catch (Exception e) {
            logger.error("set ua access control list failed, e: ", e);
            throw new JPAException("set ua access control list failed: " + e.getMessage());
        }

        try {
            while (!ua.getChainAccounts2Remove().isEmpty()) {
                ChainAccount ca2Remove = ua.getChainAccounts2Remove().peek();
                chainAccountTableJPA.deleteById(ca2Remove.getId());
                ua.getChainAccounts2Remove().remove(ca2Remove);
            }
        } catch (Exception e) {
            throw new JPAException("Remove chain account failed: " + e.getMessage());
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

    public void initAdminUA(String username, String password) throws AccountManagerException {
        setAdminName(username);
        UniversalAccount admin;
        try {
            admin = getUA(username);

            logger.info("Found adminUA. Check: {}", username);
            if (!admin.getUsername().equals(username)) {
                throw new UndefinedErrorException("Invalid adminUA username: " + username);
            }

            if (!admin.getPassword()
                    .equals(PassWordUtility.mixPassWithSalt(password, admin.getSalt()))) {
                System.out.println("Invalid adminUA password, please check.");
                throw new UndefinedErrorException("Invalid adminUA password, please check.");
            }

        } catch (AccountManagerException e) {
            if (e.getErrorCode() == ErrorCode.UAAccountNotExist.getErrorCode()) {
                // not found
                logger.info("AdminUA not found. Generate: {}", username);
                admin = UniversalAccountBuilder.newUA(username, password);
                setUA(admin);
                logger.info("AdminUA generate success!");
            }
        }
    }

    public String[] getAccountNames() {
        return universalAccountTableJPA.findUsernames().toArray(new String[0]);
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

    public void setUniversalAccountACLTableJPA(
            UniversalAccountACLTableJPA universalAccountACLTableJPA) {
        this.universalAccountACLTableJPA = universalAccountACLTableJPA;
    }
}
