package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.db.ChainAccountTableBean;
import com.webank.wecross.account.service.db.UniversalAccountTableBean;
import com.webank.wecross.account.service.exception.AccountManagerException;

import java.util.LinkedList;
import java.util.List;

public class UniversalAccountBuilder {
    public static UniversalAccount build(UniversalAccountTableBean tableBean, List<ChainAccountTableBean> chainAccountTableBeans) throws AccountManagerException {
        List<ChainAccount> chainAccounts = new LinkedList<>();
        for (ChainAccountTableBean chainAccountTableBean :
                chainAccountTableBeans) {
            ChainAccount chainAccount = ChainAccountBuilder.buildFromTableBean(chainAccountTableBean);
            chainAccounts.add(chainAccount);
        }

        UniversalAccount ua =
                UniversalAccount.builder()
                        .id(tableBean.getId())
                        .username(tableBean.getUsername())
                        .pubKey(tableBean.getPub())
                        .uaID(tableBean.getUaID())
                        .role(tableBean.getRole())
                        .password(tableBean.getPassword())
                        .secKey(tableBean.getSec())
                        .build();
        ua.setChainAccounts(chainAccounts);
        return ua;
    }
}
