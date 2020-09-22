package com.webank.wecross.account.service.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversalAccountTableJPA extends JpaRepository<UniversalAccountTableBean, Long> {
    UniversalAccountTableBean findByUsername(String username);
}
