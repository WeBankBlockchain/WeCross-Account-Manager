package com.webank.wecross.account.service.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChainAccountTableJPA extends JpaRepository<ChainAccountTableBean, Long> {
    List<ChainAccountTableBean> findByUsernameOrderByKeyIDDesc(String username);

    List<ChainAccountTableBean> findByIdentityOrderByKeyIDDesc(String identity);
}
