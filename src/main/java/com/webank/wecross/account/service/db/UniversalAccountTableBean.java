package com.webank.wecross.account.service.db;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
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

    private String email;

    // secret
    @Column(nullable = false)
    private String password;

    // secret salt
    @Column(nullable = false)
    private String salt;

    // secret
    @Column(nullable = false)
    @Convert(converter = TokenSecKeyEntryConverter.class)
    private String tokenSec;

    @Column(nullable = false, columnDefinition = "text")
    @Convert(converter = SecKeyEntryConverter.class)
    private String sec;

    @Column private String role;

    @Column(columnDefinition = "text")
    private String ext;

    @Column private Integer latestKeyID;

    @Version private Long version;

    @Column private Long updateTimestamp;
}
