package com.webank.wecross.account.service.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogoutTokenTableJPA extends JpaRepository<LogoutTokenTableBean, Long> {
    LogoutTokenTableBean findByToken(String token);
}
