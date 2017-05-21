package com.ilya.ivanov.security.authentication;

import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ilya on 5/21/17.
 */
public class DefaultAuthenticationServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final String email = "email@email.com";

    private final String password = "password";

    private final UserEntity userEntity = new UserEntity(email, password);

    private DefaultAuthenticationService SUT;


    @Before
    public void setUp() throws Exception {
        SUT = new DefaultAuthenticationService(userRepository, passwordEncoder);
    }

    @Test
    public void authenticate() throws Exception {
        when(userRepository.findByEmail(email)).thenReturn(userEntity);
        when(passwordEncoder.matches(password, userEntity.getPassword())).thenReturn(true);

        Optional<UserEntity> authenticate = SUT.authenticate(email, password);

        assertTrue(authenticate.isPresent());
        assertThat(authenticate.get(), is(userEntity));
    }

    @Test
    public void authenticateWithNotExistingEmail() throws Exception {
        when(userRepository.findByEmail(email)).thenReturn(null);
        Optional<UserEntity> authenticate = SUT.authenticate(email, password);
        assertFalse(authenticate.isPresent());
    }

    @Test
    public void authenticateWithWrongPassword() throws Exception {
        when(userRepository.findByEmail(email)).thenReturn(userEntity);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        Optional<UserEntity> authenticate = SUT.authenticate(email, password);

        assertFalse(authenticate.isPresent());
    }
}