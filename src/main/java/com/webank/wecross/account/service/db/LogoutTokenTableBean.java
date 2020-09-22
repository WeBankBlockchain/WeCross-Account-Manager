package com.webank.wecross.account.service.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_logout_token")
public class LogoutTokenTableBean {
    @Id @GeneratedValue private Integer id;

    @Column(nullable = false, unique = true)
    private String token; // token without prefix 'Bearer'
}
