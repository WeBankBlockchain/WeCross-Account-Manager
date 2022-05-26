package com.webank.wecross.account.service.db;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_chain_accounts")
public class ChainAccountTableBean {

    @Id @GeneratedValue private Integer id;

    @Column(nullable = false)
    private String username; // ua

    @Column(nullable = false)
    private Integer keyID;

    @Column(nullable = false, columnDefinition = "text")
    private String identity; // identity couldn't set to unique because of too long

    @Column(nullable = false, unique = true)
    private String identityDetail; // use this column for unique identity

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private boolean isDefault;

    @Column(nullable = false)
    private String fabricDefault;

    @Column(nullable = false, columnDefinition = "text")
    private String pub;

    @Column(nullable = false, columnDefinition = "text")
    @Convert(converter = SecKeyEntryConverter.class)
    private String sec;

    @Column(columnDefinition = "text")
    private String ext0;

    @Column(columnDefinition = "text")
    private String ext1;

    @Column(columnDefinition = "text")
    private String ext2;

    @Column(columnDefinition = "text")
    private String ext3;
}
