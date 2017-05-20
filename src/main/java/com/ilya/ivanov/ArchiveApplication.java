package com.ilya.ivanov;

import com.ilya.ivanov.data.model.Role;
import com.ilya.ivanov.data.model.UserDto;
import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySources;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.beanvalidation.CustomValidatorBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootApplication
//@EntityScan( basePackages = {"com.ilya.ivanov.data.model"} )
public class ArchiveApplication {
	private static final Logger log = Logger.getLogger(ArchiveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ArchiveApplication.class, args);
	}

	@Bean
    @Profile("dev")
    public CommandLineRunner development(UserRepository repository, Environment env, Validator validator) {
	    return (args) -> {
			UserEntity userEntity = new UserEntity("email", "pass", Role.ADMIN);
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

    @Bean
    @Profile("prod")
    public CommandLineRunner production(UserRepository repository, PasswordEncoder encoder) {
        return (args) -> {
            String password = encoder.encode("ilya");
            UserEntity admin = new UserEntity("com.ilya.ivanov@gmail.com", password, Role.ADMIN);
            if (!repository.exists(Example.of(admin)))
                repository.save(admin);
        };
    }
}
