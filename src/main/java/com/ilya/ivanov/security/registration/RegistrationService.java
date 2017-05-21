package com.ilya.ivanov.security.registration;

import com.ilya.ivanov.data.model.UserDto;
import com.ilya.ivanov.data.model.UserEntity;

import java.util.Optional;

/**
 * Created by ilya on 5/21/17.
 */
public interface RegistrationService {
    Optional<UserEntity> register(UserDto user) throws Exception;
}
