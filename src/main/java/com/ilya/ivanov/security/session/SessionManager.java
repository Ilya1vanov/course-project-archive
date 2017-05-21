package com.ilya.ivanov.security.session;

import com.ilya.ivanov.data.model.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Created by ilya on 5/21/17.
 */
@Component
public class SessionManager {
    private Session session;

    public Session newSession(UserEntity userEntity) {
        invalidateSession();
        return new Session(userEntity);
    }

    public void invalidateSession() {
        if (hasValidSession())
            session.invalidate();
    }

    public Session getSession() {
        if (session != null && session.isValid())
            return session;
        else
            throw new IllegalStateException("Invalid session");
    }

    public boolean hasValidSession() {
        return session != null && session.isValid();
    }
}
