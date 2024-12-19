package com.ftpl.finfactor.reporting.configuration;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Value("classpath:app.properties")
    private Resource fsresource;

    @Value("classpath:pfmsource.properties")
    private Resource pfmresource;

    @Bean(name = "finsenseDataSource")
    public DataSource finsenseDataSource() throws IOException {
        Properties properties= PropertiesLoaderUtils.loadProperties(fsresource);
        HikariConfig hikariConfig=new HikariConfig(properties);
        return new HikariDataSource(hikariConfig);

    }

    @Bean(name = "pfmDataSource")
    public DataSource pfmDataSource() throws IOException {
        Properties properties= PropertiesLoaderUtils.loadProperties(pfmresource);
        HikariConfig hikariConfig=new HikariConfig(properties);
        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = "finsenseJdbcTemplate")
    public JdbcTemplate finsenseJdbcTemplate(@Qualifier("finsenseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "pfmJdbcTemplate")
    public JdbcTemplate pfmJdbcTemplate(@Qualifier("pfmDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
