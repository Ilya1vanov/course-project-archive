package com.ilya.ivanov.service.user;

import com.ilya.ivanov.config.AppConfig;
import com.ilya.ivanov.data.model.user.UserDto;
import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import com.ilya.ivanov.security.registration.CredentialsPolicy;
import com.ilya.ivanov.security.registration.RegistrationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ilya on 5/21/17.
 */
@ActiveProfiles("dev")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {AppConfig.class})
public class RegistrationServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final CredentialsPolicy credentialsPolicy = mock(CredentialsPolicy.class);

    private final String email = "email@email.com";

    private final String password = "password";

    private final UserDto userDto = new UserDto(email, password, password);

    private final UserEntity userEntity = new UserEntity(email, password);

    private RegistrationService SUT;

    @Before
    public void setUp() throws Exception {
        SUT = new DefaultUserService(userRepository, passwordEncoder, credentialsPolicy);
        when(credentialsPolicy.alwaysGenerateOnRegistration()).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(password);
    }

    @Test
    public void register() throws Exception {
        when(userRepository.findByEmail(email)).thenReturn(null);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        Optional<UserEntity> register = SUT.register(userDto);

        assertTrue(register.isPresent());
        assertThat(register.get(), is(userEntity));
    }

    @Test
    public void registerWithExistingEmail() throws Exception {
        when(userRepository.findByEmail(email)).thenReturn(userEntity);
        Optional<UserEntity> register = SUT.register(userDto);

        assertFalse(register.isPresent());
    }
}