package com.webank.wecross.account.service.config;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfig {
    @Resource ApplicationConfig config;

    @Bean
    public DataSource newDataSource() throws Exception {
        /*
        DataSource dataSource =
                new DriverManagerDataSource(
                        config.getDb().getUrl(),
                        config.getDb().getUsername(),
                        config.getDb().getPassword());

        */
        Map<String, String> map = new HashMap();
        map.put("url", config.getDb().getUrl());
        map.put("username", config.getDb().getUsername());
        map.put("password", config.getDb().getPassword());

        DataSource dataSource = DruidDataSourceFactory.createDataSource(map);
        return dataSource;
    }
}
