package com.ilya.ivanov.security.session;

import org.springframework.context.ApplicationEvent;

/**
 * Created by ilya on 6/5/17.
 */
public class InvalidateSessionEvent extends ApplicationEvent {
    public InvalidateSessionEvent(Object source) {
        super(source);
    }
}
