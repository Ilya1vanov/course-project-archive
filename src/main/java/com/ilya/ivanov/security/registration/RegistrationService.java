package com.ilya.ivanov.security.registration;

import com.ilya.ivanov.data.model.user.Role;
import com.ilya.ivanov.data.model.user.UserDto;
import com.ilya.ivanov.data.model.user.UserEntity;

import java.util.Optional;

/**
 * Created by ilya on 5/21/17.
 */
public interface RegistrationService {
    Optional<UserEntity> register(UserDto user) throws Exception;

    Optional<UserEntity> register(UserDto user, Role role) throws Exception;
}
