package com.ilya.ivanov.security.session;

import com.google.common.collect.ImmutableList;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.model.user.UserEntity;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ilya on 5/20/17.
 */
public class Session {
    private UserEntity userEntity;

    private boolean valid;

    public Session(UserEntity userEntity) {
        this.userEntity = userEntity;
        this.valid = true;
    }

    public UserEntity getUserEntity() {
        checkValidity();
        return userEntity;
    }

    boolean isValid() {
        return valid;
    }

    private void checkValidity() {
        if (!valid)
            throw new IllegalStateException("Session is invalid");
    }

    void invalidate() {
        this.valid = false;
    }
}
