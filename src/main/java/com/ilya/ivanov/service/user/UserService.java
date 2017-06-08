package com.ilya.ivanov.service.user;

import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.security.authentication.AuthenticationService;
import com.ilya.ivanov.security.registration.RegistrationService;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ilya on 6/7/17.
 */
public interface UserService extends RegistrationService, AuthenticationService, AutocompletionService {
    Set<UserEntity> getAdmins();
}
