package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.db.UniversalAccountACLTableBean;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountAccessControlList {
    private String username;
    private List<String> allowChainPaths;
    private Long updateTimestamp;

    public static AccountAccessControlList buildFromTableBean(
            UniversalAccountACLTableBean universalAccountACLTableBean) {
        return AccountAccessControlList.builder()
                .username(universalAccountACLTableBean.getUsername())
                .allowChainPaths(universalAccountACLTableBean.getAllowChainPaths())
                .updateTimestamp(universalAccountACLTableBean.getUpdateTimestamp())
                .build();
    }
}
