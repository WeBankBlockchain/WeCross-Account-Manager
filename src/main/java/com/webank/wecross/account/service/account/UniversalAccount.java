package com.webank.wecross.account.service.account;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.webank.wecross.account.service.db.UniversalAccountACLTableBean;
import com.webank.wecross.account.service.db.UniversalAccountTableBean;
import com.webank.wecross.account.service.exception.AccountManagerException;
import com.webank.wecross.account.service.exception.ErrorCode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniversalAccount {
    @JsonIgnore private Integer id;

    private String username;
    private String email;
    private String uaID;
    private String pubKey;
    private boolean isAdmin;

    private String[] allowChainPaths;
    @JsonIgnore private Integer aclId;

    @JsonIgnore private String password;

    @JsonIgnore private String salt;

    @JsonIgnore private String tokenSec;

    @JsonIgnore private String secKey;

    @JsonIgnore private String role;

    @JsonIgnore private Map<String, Map<Integer, ChainAccount>> type2ChainAccounts;

    @JsonIgnore private Queue<ChainAccount> chainAccounts2Remove;

    @JsonIgnore private int latestKeyID;

    private final Long version; // The state of ua, update by db automatically

    @JsonSetter("chainAccounts")
    public void setChainAccounts(List<ChainAccount> chainAccounts) {
        if (type2ChainAccounts == null) {
            type2ChainAccounts = new HashMap<>();
        }

        for (ChainAccount chainAccount : chainAccounts) {
            String type = chainAccount.getType();
            type2ChainAccounts.putIfAbsent(type, new LinkedHashMap<>());
            type2ChainAccounts.get(type).putIfAbsent(chainAccount.getKeyID(), chainAccount);
        }
    }

    @JsonGetter("chainAccounts")
    public List<ChainAccount> getChainAccounts() {
        if (type2ChainAccounts == null) {
            type2ChainAccounts = new HashMap<>();
        }

        List<ChainAccount> chainAccounts = new LinkedList<>();
        for (Map<Integer, ChainAccount> chainAccountMap : type2ChainAccounts.values()) {
            for (ChainAccount chainAccount : chainAccountMap.values()) {
                chainAccounts.add(chainAccount);
            }
        }
        return chainAccounts;
    }

    public void addChainAccount(ChainAccount chainAccount) throws AccountManagerException {
        if (isChainAccountExist(chainAccount)) {
            throw new AccountManagerException(
                    ErrorCode.ChainAccountExist.getErrorCode(), "chain account exists");
        }
        if (type2ChainAccounts == null) {
            type2ChainAccounts = new HashMap<>();
        }

        type2ChainAccounts.putIfAbsent(chainAccount.getType(), new LinkedHashMap<>());
        Map<Integer, ChainAccount> chainAccountMap = type2ChainAccounts.get(chainAccount.getType());
        if (chainAccount.isDefault) {
            for (ChainAccount ca : chainAccountMap.values()) {
                ca.setDefault(false);
            }
        }
        if (chainAccountMap.size() == 0) {
            chainAccount.setDefault(true);
        }

        chainAccount.setKeyID(latestKeyID++);
        chainAccountMap.putIfAbsent(chainAccount.getKeyID(), chainAccount);
    }

    public void removeChainAccount(Integer id, String type) throws AccountManagerException {
        if (type2ChainAccounts == null) {
            throw new AccountManagerException(
                    ErrorCode.ChainAccountNotExist.getErrorCode(),
                    "Chain account not found, id: " + id + " type: " + type);
        }

        if (!type2ChainAccounts.containsKey(type)) {
            throw new AccountManagerException(
                    ErrorCode.ChainAccountNotExist.getErrorCode(),
                    "Chain account not found, id: " + id + " type: " + type);
        }

        Map<Integer, ChainAccount> chainAccountMap = type2ChainAccounts.get(type);
        ChainAccount caRemoved = chainAccountMap.remove(id);
        if (caRemoved == null) {
            throw new AccountManagerException(
                    ErrorCode.ChainAccountNotExist.getErrorCode(),
                    "Chain account not found, id: " + id + " type: " + type);
        } // else remove success

        if (caRemoved.isDefault) {
            // Choose smallest id to be the next default
            ChainAccount ca2Default = null;
            for (ChainAccount ca : chainAccountMap.values()) {
                if (ca2Default == null || ca2Default.getId() > ca.getId()) {
                    ca2Default = ca;
                }
            }

            if (ca2Default != null) {
                ca2Default.setDefault(true);
            }
        }

        // Add 2 removed list
        getChainAccounts2Remove().offer(caRemoved);
    }

    public void setChainAccount(ChainAccount chainAccount) throws AccountManagerException {
        if (!isChainAccountExist(chainAccount)) {
            throw new AccountManagerException(
                    ErrorCode.ChainAccountNotExist.getErrorCode(), "chain account not exists");
        }

        if (type2ChainAccounts == null) {
            type2ChainAccounts = new HashMap<>();
        }

        type2ChainAccounts.putIfAbsent(chainAccount.getType(), new LinkedHashMap<>());

        Map<Integer, ChainAccount> chainAccountMap = type2ChainAccounts.get(chainAccount.getType());
        for (ChainAccount ca : chainAccountMap.values()) {
            if (ca.equals(chainAccount)) {
                continue;
            }

            if (chainAccount.isDefault) {
                ca.setDefault(false);
            }

            if (ca.pubKey.equals(chainAccount.pubKey)) {
                ca.username = chainAccount.username;
                // ca.keyID = chainAccount.keyID;
                ca.type = chainAccount.type;
                ca.isDefault = chainAccount.isDefault;
                ca.pubKey = chainAccount.pubKey;
                ca.secKey = chainAccount.secKey;
                ca.ext0 = chainAccount.ext0;
                ca.ext1 = chainAccount.ext1;
                ca.ext2 = chainAccount.ext2;
                ca.ext3 = chainAccount.ext3;
            }
        }
    }

    public boolean isChainAccountExist(ChainAccount chainAccount) {
        if (type2ChainAccounts == null) {
            return false;
        }

        type2ChainAccounts.putIfAbsent(chainAccount.getType(), new LinkedHashMap<>());
        Map<Integer, ChainAccount> chainAccountMap = type2ChainAccounts.get(chainAccount.getType());
        for (ChainAccount ca : chainAccountMap.values()) {
            if (ca.getIdentityDetail().equals(chainAccount.getIdentityDetail())) {
                return true;
            }
        }
        return false;
    }

    public ChainAccount getChainAccountByKeyID(Integer keyID) {
        if (type2ChainAccounts == null) {
            return null;
        }

        for (Map<Integer, ChainAccount> chainAccountMap : type2ChainAccounts.values()) {
            for (ChainAccount ca : chainAccountMap.values()) {
                if (ca.keyID.equals(keyID)) {
                    return ca;
                }
            }
        }
        return null;
    }

    public Queue<ChainAccount> getChainAccounts2Remove() {
        if (chainAccounts2Remove == null) {
            chainAccounts2Remove = new LinkedList<>();
        }
        return chainAccounts2Remove;
    }

    @Data
    public class Info {
        private String username;
        private String uaID;
        private String pubKey;

        @JsonProperty("isAdmin")
        private boolean isAdmin;
    }

    public Info toInfo() {
        Info info = new Info();
        info.setUsername(this.username);
        info.setUaID(this.uaID);
        info.setPubKey(this.pubKey);
        info.setAdmin(isAdmin);
        return info;
    }

    @Data
    public class Details {
        private String username;
        private String uaID;
        private String pubKey;
        private String password;
        private String secKey;
        private String role;
        private String[] allowChainPaths;
        private Long version;

        @JsonProperty("isAdmin")
        private boolean isAdmin;

        private Map<String, Map<Integer, ChainAccount.Details>> type2ChainAccountDetails;
    }

    public Details toDetails() {
        Details details = new Details();
        details.setUsername(username);
        details.setUaID(uaID);
        details.setPubKey(pubKey);
        details.setPassword(password);
        details.setSecKey(secKey);
        details.setRole(role);
        details.setAdmin(isAdmin);
        details.setAllowChainPaths(allowChainPaths);
        details.setVersion(version);

        Map<String, Map<Integer, ChainAccount.Details>> type2ChainAccountDetails = new HashMap<>();
        for (Map.Entry<String, Map<Integer, ChainAccount>> t2cas : type2ChainAccounts.entrySet()) {
            Map<Integer, ChainAccount.Details> chainAccountDetails = new HashMap<>();
            String type = t2cas.getKey();
            for (Map.Entry<Integer, ChainAccount> id2cas : t2cas.getValue().entrySet()) {
                chainAccountDetails.putIfAbsent(id2cas.getKey(), id2cas.getValue().toDetails());
            }
            type2ChainAccountDetails.putIfAbsent(type, chainAccountDetails);
        }

        details.setType2ChainAccountDetails(type2ChainAccountDetails);

        return details;
    }

    public UniversalAccountTableBean toTableBean() {
        UniversalAccountTableBean tableBean = new UniversalAccountTableBean();
        tableBean.setId(id);
        tableBean.setUsername(username);
        tableBean.setEmail(email);
        tableBean.setUaID(uaID);
        tableBean.setPub(pubKey);
        tableBean.setPassword(password);
        tableBean.setSalt(salt);
        tableBean.setTokenSec(tokenSec);
        tableBean.setSec(secKey);
        tableBean.setRole(role);
        tableBean.setLatestKeyID(latestKeyID);
        tableBean.setVersion(version);
        return tableBean;
    }

    public UniversalAccountACLTableBean toACLBean() {
        List<String> paths = null;
        if (allowChainPaths != null) {
            paths = Arrays.asList(allowChainPaths);
        }

        UniversalAccountACLTableBean tableBean = new UniversalAccountACLTableBean();
        tableBean.setId(aclId);
        tableBean.setUsername(username);
        tableBean.setAllowChainPaths(paths);

        return tableBean;
    }
}
