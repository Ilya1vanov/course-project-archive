package com.ilya.ivanov.service.user;

import java.util.Collection;

/**
 * Created by ilya on 5/31/17.
 */
public interface AutocompletionService {
    Collection<String> getAutocompletion(String text);
}
