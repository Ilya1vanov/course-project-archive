package com.ilya.ivanov.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
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
@EnableJpaRepositories("com.ilya.ivanov.data.repository")
@PropertySource("classpath:db.properties")
public class JpaConfig {
    private final Environment env;

//    private final String database;

    @Autowired
    public JpaConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @Profile("dev")
    public DataSource embeddedDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
//                .addScript("classpath:test-data.sql")
                .build();
    }

    @Bean
    @Profile("prod")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
//        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
//        dataSource.setUrl(env.getProperty("spring.datasource.url"));
//        dataSource.setUsername(env.getProperty("spring.datasource.username"));
//        dataSource.setPassword(env.getProperty("spring.datasource.password"));
//        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        if (env.acceptsProfiles("prod", "QA"))
            vendorAdapter.setDatabase(Database.valueOf(env.getProperty("db.type")));
        else if (env.acceptsProfiles("dev", "test"))
            vendorAdapter.setDatabase(Database.H2);

        vendorAdapter.setGenerateDdl(env.getProperty("spring.jpa.generate-ddl", Boolean.class));
        vendorAdapter.setShowSql(env.getProperty("spring.jpa.show-sql", Boolean.class));

        LocalContainerEntityManagerFactoryBean emFactory = new LocalContainerEntityManagerFactoryBean();
        emFactory.setJpaVendorAdapter(vendorAdapter);
        emFactory.setPackagesToScan("server.spring.data.model");

        if (env.acceptsProfiles("prod", "QA"))
            emFactory.setDataSource(dataSource());
        else if (env.acceptsProfiles("dev", "test"))
            emFactory.setDataSource(embeddedDataSource());

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
