package com.webank.wecross.account.service.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginTokenTableJPA extends JpaRepository<LoginTokenTableBean, Integer> {
    LoginTokenTableBean findByToken(String token);
}
