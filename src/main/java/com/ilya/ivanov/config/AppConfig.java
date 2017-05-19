package com.ilya.ivanov.config;

import com.ilya.ivanov.data.aspects.Encoding;
import com.ilya.ivanov.data.model.FileEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by ilya on 5/19/17.
 */
@Configuration
public class AppConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public FileEntity placeholder(FileEntity parent) {
        return new FileEntity(parent);
    }
}
