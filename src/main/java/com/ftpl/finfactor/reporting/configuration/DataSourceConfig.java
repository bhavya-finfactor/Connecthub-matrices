package com.ftpl.finfactor.reporting.configuration;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Bean(name = "finsenseDataSourceProperties")
    @ConfigurationProperties(prefix = "finsense.datasource")
    public HikariConfig finsenseDataSourceProperties() {
        return new HikariConfig();
    }

    @Bean(name = "pfmDataSourceProperties")
    @ConfigurationProperties(prefix = "pfm.datasource")
    public HikariConfig pfmDataSourceProperties() {
        return new HikariConfig();
    }

    @Bean(name = "chDataSourceProperties")
    @ConfigurationProperties(prefix = "ch.datasource")
    public HikariConfig chDataSourceProperties() {
        return new HikariConfig();
    }


    @Bean(name = "finsenseDataSource")
    public DataSource finsenseDataSource(@Qualifier("finsenseDataSourceProperties") HikariConfig hikariConfig) throws IOException {
        return new HikariDataSource(hikariConfig);

    }

    @Bean(name = "chDataSource")
    public DataSource chDataSource(@Qualifier("chDataSourceProperties") HikariConfig hikariConfig) throws IOException {
        return new HikariDataSource(hikariConfig);

    }


    @Bean(name = "pfmDataSource")
    public DataSource pfmDataSource(@Qualifier("pfmDataSourceProperties") HikariConfig config) throws IOException {
        return new HikariDataSource(config);
    }

}
