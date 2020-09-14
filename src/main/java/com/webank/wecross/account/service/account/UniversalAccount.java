package com.webank.wecross.account.service.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecross.account.service.db.UniversalAccountTableBean;
import com.webank.wecross.account.service.exception.AddChainAccountException;
import com.webank.wecross.account.service.exception.SetChainAccountException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniversalAccount {
    @JsonIgnore private Integer id;

    private String username;
    private String uaID;
    private String pubKey;

    @JsonIgnore private String password;

    @JsonIgnore private String secKey;

    @JsonIgnore private String role;

    @JsonIgnore private Map<String, Map<Integer, ChainAccount>> type2ChainAccounts;

    @Setter
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

    @Getter
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

    public void addChainAccount(ChainAccount chainAccount) throws AddChainAccountException {
        if (isChainAccountExist(chainAccount)) {
            throw new AddChainAccountException("chain account exists");
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

        chainAccount.setKeyID(new Integer(getChainAccounts().size()));
        chainAccountMap.putIfAbsent(chainAccount.getKeyID(), chainAccount);
    }

    public void setChainAccount(ChainAccount chainAccount) throws SetChainAccountException {
        if (!isChainAccountExist(chainAccount)) {
            throw new SetChainAccountException("chain account not exists");
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
                ca.UAProof = chainAccount.UAProof;
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
            if (ca.pubKey.equals(chainAccount.pubKey)) {
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

    @Data
    public class Info {
        private String username;
        private String uaID;
        private String pubKey;
    }

    public Info toInfo() {
        Info info = new Info();
        info.setUsername(this.username);
        info.setUaID(this.uaID);
        info.setPubKey(this.pubKey);
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
        tableBean.setUaID(uaID);
        tableBean.setPub(pubKey);
        tableBean.setPassword(password);
        tableBean.setSec(secKey);
        tableBean.setRole(role);
        return tableBean;
    }
}
