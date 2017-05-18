package com.ilya.ivanov;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EntityScan( basePackages = {"com.ilya.ivanov.data.model"} )
public class ArchiveApplication {
	public static void main(String[] args) {
		SpringApplication.run(ArchiveApplication.class, args);
	}

	@Bean
    public CommandLineRunner runner() {
	    return (args) -> {

        };
    }


}
