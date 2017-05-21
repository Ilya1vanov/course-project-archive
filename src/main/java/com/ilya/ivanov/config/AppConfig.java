package com.ilya.ivanov.config;

import com.ilya.ivanov.ArchiveApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.context.annotation.ComponentScan.Filter;

/**
 * Created by ilya on 5/19/17.
 */
@Configuration
//@ComponentScan(basePackages = {"com.ilya.ivanov"}, excludeFilters = {
//    @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ArchiveApplication.class),
//    @Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${spring.profiles.active}.properties")
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
}
