package com.ilya.ivanov.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

/**
 * Created by ilya on 5/21/17.
 */
@Configuration
@ComponentScan(basePackages = "com.ilya.ivanov.aspect")
@EnableSpringConfigured
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AspectConfig {
}
