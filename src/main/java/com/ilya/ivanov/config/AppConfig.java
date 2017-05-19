package com.ilya.ivanov.config;

import com.ilya.ivanov.data.model.FileEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by ilya on 5/19/17.
 */
@Configuration
@EnableSpringConfigured
@PropertySource("classpath:application.properties")
public class AppConfig {
    /**
     * {@link PropertySourcesPlaceholderConfigurer} have to define as static
     * method so that can be initialized earlier on container startup time.
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public FileEntity placeholder(FileEntity parent) {
        return new FileEntity(parent);
    }
}
