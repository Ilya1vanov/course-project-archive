package com.ilya.ivanov.data.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by ilya on 5/19/17.
 */
@Aspect
@Component("encoding")
public class Encoding {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public Encoding(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Pointcut("execution(* com.ilya.ivanov.data.model.UserEntity.setPassword(..)) ")
    public void performance() {}

    @Before("performance()")
    public void before() {
        System.out.println("Before");
    }

    @Around("performance()")
    public void watchPerformance(ProceedingJoinPoint jp) {
        try {
            System.out.println("Encoding");
            Object[] args = jp.getArgs();
            args[0] = passwordEncoder.encode(args[0].toString());
            jp.proceed(args);
        } catch (Throwable e) {
            System.out.println("Demanding a refund");
        }
    }
}
