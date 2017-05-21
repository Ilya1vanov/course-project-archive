package com.ilya.ivanov.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by ilya on 5/18/17.
 */
@Configuration
@EnableTransactionManagement
@EnableAutoConfiguration
@Import(AppConfig.class)
@EnableJpaRepositories("com.ilya.ivanov.data.repository")
@PropertySource("classpath:db.properties")
public class JpaConfig {
    private final Environment env;

    @Autowired
    public JpaConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(env.getProperty("spring.jpa.database", Database.class));

        vendorAdapter.setGenerateDdl(env.getProperty("spring.jpa.generate-ddl", Boolean.class));
        vendorAdapter.setShowSql(env.getProperty("spring.jpa.show-sql", Boolean.class));

        LocalContainerEntityManagerFactoryBean emFactory = new LocalContainerEntityManagerFactoryBean();
        emFactory.setJpaVendorAdapter(vendorAdapter);
        emFactory.setPackagesToScan("com.ilya.ivanov.data.model");

        emFactory.setDataSource(dataSource());

        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        jpaProperties.setProperty("hibernate.max_fetch_depth", env.getProperty("db.max_fetch_depth"));
        jpaProperties.setProperty("hibernate.jdbc.fetch_size", env.getProperty("db.jdbc.fetch_size"));
        jpaProperties.setProperty("hibernate.jdbc.batch_size", env.getProperty("db.jdbc.batch_size"));

        emFactory.setJpaProperties(jpaProperties);

        return emFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}
