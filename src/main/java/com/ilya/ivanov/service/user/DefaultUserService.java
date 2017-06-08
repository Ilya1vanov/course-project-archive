package com.ilya.ivanov.service.user;

import com.ilya.ivanov.data.model.user.Role;
import com.ilya.ivanov.data.model.user.UserDto;
import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import com.ilya.ivanov.security.authentication.AuthenticationService;
import com.ilya.ivanov.security.registration.CredentialsPolicy;
import com.ilya.ivanov.security.registration.PasswordGenerator;
import com.ilya.ivanov.security.registration.PropertiesFileCredentialsPolicy;
import com.ilya.ivanov.security.registration.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ilya on 5/31/17.
 */
@Component
public class DefaultUserService implements UserService {
    @Value("${com.ilya.ivanov.registration.defaultRole}")
    private Role defaultRole;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private CredentialsPolicy credentialsPolicy;

    private PasswordGenerator passwordGenerator;

    @Autowired
    public DefaultUserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CredentialsPolicy credentialsPolicy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.credentialsPolicy = credentialsPolicy;
    }

    @Override
    public Optional<UserEntity> register(UserDto user) throws Exception {
        return register(user, defaultRole);
    }

    @Override
    public Optional<UserEntity> register(UserDto user, Role role) throws Exception {
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
        return Optional.ofNullable(userRepository.save(new UserEntity(user.getEmail(), encodedPassword, role)));
    }


    public void setCredentialsPolicy(CredentialsPolicy credentialsPolicy) {
        this.credentialsPolicy = credentialsPolicy;
    }

    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Optional<UserEntity> authenticate(String email, String password) {
        UserEntity byEmailAndPassword = userRepository.findByEmail(email);
        if (byEmailAndPassword != null && passwordEncoder.matches(password, byEmailAndPassword.getPassword()))
            return Optional.of(byEmailAndPassword);
        else
            return Optional.empty();
    }

    @Override
    public Collection<String> getAutocompletion(String text) {
        return userRepository
                .findByEmailStartsWith(text)
                .stream()
                .map(UserEntity::getEmail)
                .collect(Collectors.toList());
    }

    @Override
    public Set<UserEntity> getAdmins() {
        return userRepository.findAllByRole(Role.ADMIN);
    }
}
