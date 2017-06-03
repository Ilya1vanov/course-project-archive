package com.ilya.ivanov.security.authentication;

import com.ilya.ivanov.data.model.user.UserEntity;

import java.util.Optional;

/**
 * Created by ilya on 5/21/17.
 */
public interface AuthenticationService {
    Optional<UserEntity> authenticate(String email, String password);
}
