package com.atguigu.gmall.sms.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置
 *
 *
 */
@Configuration
public class DataSourceConfig {

    /**
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     * driver-class-name: com.mysql.jdbc.Driver
     *     url: jdbc:mysql://192.168.239.129:3306/guli_pms?characterEncoding=utf-8&serverTimezone=GMT%2B8
     *     username: root
     *     password: root
     * @param
     * @return The default datasource
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource(
            @Value("${spring.datasource.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.url}") String url
    ) {

        HikariDataSource hikariDataSource= new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setDriverClassName(driverClassName);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
        return new DataSourceProxy(hikariDataSource);
    }
}
