package com.ilya.ivanov.security.session;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ilya.ivanov.data.model.file.FileDto;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.model.user.UserEntity;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ilya on 5/20/17.
 */
public class Session {
    private UserEntity userEntity;

    private LocalDateTime start;

    private LocalDateTime end;

    private boolean valid;

    private Map<ActivityType, Collection<FileDto>> activity = new HashMap<>();

    Session(UserEntity userEntity) {
        this.userEntity = userEntity;
        this.valid = true;
        this.start = LocalDateTime.now();
    }

    public void newActivity(ActivityType type, Collection<FileDto> files) {
        checkValidity();
        if (activity.containsKey(type))
            activity.get(type).addAll(files);
        else
            activity.put(type, new HashSet<>(files));
    }

    public Map<ActivityType, Collection<FileDto>> getActivity() {
        return ImmutableMap.copyOf(activity);
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    boolean isValid() {
        return valid;
    }

    private void checkValidity() {
        if (!valid)
            throw new IllegalStateException("Session is invalid");
    }

    void invalidate() {
        if (isValid()) {
            this.valid = false;
            if (this.end == null)
                this.end = LocalDateTime.now();
        }
    }
}
