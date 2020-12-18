package com.webank.wecross.account.service.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_login_token")
public class LoginTokenTableBean {
    @Id @GeneratedValue private Integer id;

    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String token; // token without prefix 'Bearer'

    @Column(nullable = false)
    private long lastActiveTimestamp;

    public void setLogout() {
        setLastActiveTimestamp(0); // current - 0 > expires (always true)
    }
}
