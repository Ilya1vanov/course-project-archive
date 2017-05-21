package com.ilya.ivanov.security.authentication;

import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by ilya on 5/21/17.
 */
@Component
public class DefaultAuthenticationService implements AuthenticationService {
    private final UserRepository userRepository;

    @Autowired
    public DefaultAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<UserEntity> authenticate(String email, String password) {
        return Optional.ofNullable(userRepository.findByEmailAndPassword(email, password));
    }
}
