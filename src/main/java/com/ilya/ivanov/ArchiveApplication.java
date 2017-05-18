package com.ilya.ivanov;

import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
@EntityScan( basePackages = {"com.ilya.ivanov.data.model"} )
public class ArchiveApplication {
	private static final Logger log = Logger.getLogger(ArchiveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ArchiveApplication.class, args);
	}

	@Bean
    public CommandLineRunner runner(Environment env) {
	    return (args) -> {
			log.info(Arrays.toString(env.getActiveProfiles()));
        };
    }


}
