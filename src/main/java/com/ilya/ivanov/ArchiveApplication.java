package com.ilya.ivanov;

import com.ilya.ivanov.data.model.Role;
import com.ilya.ivanov.data.model.UserDto;
import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@EntityScan( basePackages = {"com.ilya.ivanov.data.model"} )
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ArchiveApplication {
	private static final Logger log = Logger.getLogger(ArchiveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ArchiveApplication.class, args);
	}

	@Bean
    public CommandLineRunner runner(UserRepository repository, Environment env, Validator validator) {
	    return (args) -> {
            UserEntity userEntity = new UserEntity("email", "pass", Role.ADMIN);
            userEntity.setPassword("password");
            log.debug(userEntity.getPassword());
            repository.save(userEntity);
            List<UserEntity> all = repository.findAll();
            all.forEach(System.out::println);
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
