package com.webank.wecross.account.service.authcode;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MailCode {
    private String code;
    private int validTime;
    private LocalDateTime createTime;

    public boolean isExpired() {
        if (getCreateTime() == null || getValidTime() == 0) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(createTime.plusSeconds(validTime));
    }
}
