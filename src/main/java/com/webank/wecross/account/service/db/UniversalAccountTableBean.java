package com.webank.wecross.account.service.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_universal_accounts")
public class UniversalAccountTableBean {

    @Id @GeneratedValue private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String uaID;

    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String pub;

    // secret
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "text")
    private String sec;

    @Column private String role;

    @Column(columnDefinition = "text")
    private String ext;
}
