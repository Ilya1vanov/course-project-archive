package com.ilya.ivanov.security.session;

import com.ilya.ivanov.data.model.user.UserEntity;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by ilya on 5/21/17.
 */
@Component
public class SessionManager implements ApplicationContextAware {
    private Session session;

    private ApplicationContext context;

    public Session newSession(UserEntity userEntity) {
        if (hasValidSession())
            invalidateSession();
        this.session = new Session(userEntity);
        context.publishEvent(new NewSessionEvent(session));
        return session;
    }

    public void invalidateSession() {
        if (hasValidSession())
            session.invalidate();
    }

    public Session getSession() {
        return session;
    }

    public boolean hasValidSession() {
        return session != null && session.isValid();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
