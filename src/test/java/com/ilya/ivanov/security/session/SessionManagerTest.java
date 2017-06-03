package com.ilya.ivanov.security.session;

import com.ilya.ivanov.data.model.user.UserEntity;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ilya on 5/21/17.
 */
public class SessionManagerTest {
    private final SessionManager SUT = new SessionManager();

    private final UserEntity userEntity = new UserEntity("mail", "pass");

    @Test
    public void newSession() throws Exception {
        Session session = SUT.newSession(userEntity);
        assertNotNull(session);
    }

    @Test
    public void invalidateSession() throws Exception {
        Session session = SUT.newSession(userEntity);
        SUT.invalidateSession();
        assertFalse(SUT.hasValidSession());
    }

    @Test(expected = IllegalStateException.class)
    public void getSession() throws Exception {
        SUT.getSession();
    }

    @Test
    public void hasValidSession() throws Exception {
        assertFalse(SUT.hasValidSession());
    }
}