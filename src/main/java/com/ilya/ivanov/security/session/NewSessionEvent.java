package com.ilya.ivanov.security.session;

import org.springframework.context.ApplicationEvent;

/**
 * Created by ilya on 5/30/17.
 */
public class NewSessionEvent extends ApplicationEvent {
    public NewSessionEvent(Object source) {
        super(source);
    }
}
