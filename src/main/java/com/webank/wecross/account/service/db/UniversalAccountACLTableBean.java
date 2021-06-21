package com.webank.wecross.account.service.db;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/** The table of UA access control list */
@Data
@Entity
@Table(name = "t_ua_access_control_list")
public class UniversalAccountACLTableBean {
    @Id @GeneratedValue private Integer id;

    @Column(nullable = false, unique = true)
    private String username; // ua

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> allowChainPaths = new ArrayList<>();

    @Column private Long updateTimestamp;
}
