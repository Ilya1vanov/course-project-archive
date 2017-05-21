package com.ilya.ivanov.security.authentication;

import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by ilya on 5/21/17.
 */
@Component
public class DefaultAuthenticationService implements AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultAuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<UserEntity> authenticate(String email, String password) {
        UserEntity byEmailAndPassword = userRepository.findByEmail(email);
        if (byEmailAndPassword != null && passwordEncoder.matches(password, byEmailAndPassword.getPassword()))
            return Optional.of(byEmailAndPassword);
        else
            return Optional.empty();
    }
}
