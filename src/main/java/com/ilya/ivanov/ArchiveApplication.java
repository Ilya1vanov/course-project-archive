package com.ilya.ivanov;

import com.ilya.ivanov.data.model.UserDto;
import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Set;

@SpringBootApplication
@EntityScan( basePackages = {"com.ilya.ivanov.data.model"} )
public class ArchiveApplication {
	private static final Logger log = Logger.getLogger(ArchiveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ArchiveApplication.class, args);
	}

	@Bean
    public CommandLineRunner runner(Environment env, Validator validator) {
	    return (args) -> {
			log.info(Arrays.toString(env.getActiveProfiles()));
			UserDto dto = new UserDto();
			dto.setEmail("asd");
			dto.setPassword("s");
			dto.setPassword("a");
			Set<ConstraintViolation<UserDto>> validate = validator.validate(dto);
			validate.forEach(System.out::println);
		};
    }
}
