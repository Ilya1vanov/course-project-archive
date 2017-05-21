package com.ilya.ivanov.security.registration;

import com.ilya.ivanov.data.model.Role;
import com.ilya.ivanov.data.model.UserDto;
import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by ilya on 5/21/17.
 */
@Component
public class DefaultRegistrationService implements RegistrationService {
    @Value("${com.ilya.ivanov.registration.defaultRole}")
    private Role defaultRole;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private CredentialsPolicy credentialsPolicy;

    private PasswordGenerator passwordGenerator;

    @Autowired
    public DefaultRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, CredentialsPolicy credentialsPolicy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.credentialsPolicy = credentialsPolicy;
    }

    @Override
    public Optional<UserEntity> register(UserDto user) throws Exception {
        UserEntity existing = userRepository.findByEmail(user.getEmail());
        if (existing != null) {
            return Optional.empty();
        }

        if(credentialsPolicy == null) {
            credentialsPolicy = new PropertiesFileCredentialsPolicy();
        }

        String password = user.getPassword();

        if(credentialsPolicy.alwaysGenerateOnRegistration()) {
            if(passwordGenerator == null) {
                Class<PasswordGenerator> passwordGeneratorType =
                        credentialsPolicy.defaultPasswordGeneratorType();

                passwordGenerator = passwordGeneratorType.newInstance();
            }
            password = passwordGenerator.generate();
        }

        String encodedPassword = passwordEncoder.encode(password);
        return Optional.ofNullable(userRepository.save(new UserEntity(user.getEmail(), encodedPassword, defaultRole)));
    }


    public void setCredentialsPolicy(CredentialsPolicy credentialsPolicy) {
        this.credentialsPolicy = credentialsPolicy;
    }

    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }
}
