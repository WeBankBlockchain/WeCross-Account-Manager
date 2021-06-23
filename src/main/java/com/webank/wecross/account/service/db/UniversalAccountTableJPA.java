package com.webank.wecross.account.service.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UniversalAccountTableJPA
        extends JpaRepository<UniversalAccountTableBean, Integer> {
    UniversalAccountTableBean findByUsername(String username);

    @Query(value = "select ua.tokenSec from UniversalAccountTableBean ua where ua.username=?1")
    String findTokenSecByUsername(String username);

    @Query(value = "select ua.username from UniversalAccountTableBean ua")
    List<String> findUsernames();
}
