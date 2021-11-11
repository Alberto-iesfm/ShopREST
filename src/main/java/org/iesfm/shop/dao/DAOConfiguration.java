package org.iesfm.shop.dao;

import org.iesfm.shop.dao.inmemory.InMemoryArticleDAO;
import org.iesfm.shop.dao.jdbc.jdbcArticleDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@PropertySource("application.properties")
public class DAOConfiguration {

    @Bean
    public DataSource dataSource(
            @Value("${database.driver}") String driver,
            @Value("${database.url}") String url,
            @Value("${database.user}") String user,
            @Value("${database.password}") String password
    ) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean
    public ArticleDAO articleDAO(NamedParameterJdbcTemplate jdbcTemplate){
        return new jdbcArticleDAO(jdbcTemplate);
    }
}
