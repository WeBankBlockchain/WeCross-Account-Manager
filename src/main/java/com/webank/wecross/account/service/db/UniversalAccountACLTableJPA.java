package com.webank.wecross.account.service.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversalAccountACLTableJPA
        extends JpaRepository<UniversalAccountACLTableBean, Integer> {
    UniversalAccountACLTableBean findByUsername(String username);
}
