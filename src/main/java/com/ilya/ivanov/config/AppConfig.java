package com.ilya.ivanov.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by ilya on 5/19/17.
 */
@Configuration
@PropertySource("classpath:application.yaml")
@EnableConfigurationProperties
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
