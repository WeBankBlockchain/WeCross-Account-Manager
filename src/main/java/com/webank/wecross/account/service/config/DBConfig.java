package com.webank.wecross.account.service.config;

import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DBConfig {
    @Resource ApplicationConfig config;

    @Bean
    public DataSource newDataSource() {
        DataSource dataSource =
                new DriverManagerDataSource(config.db.url, config.db.username, config.db.password);
        return dataSource;
    }
}
