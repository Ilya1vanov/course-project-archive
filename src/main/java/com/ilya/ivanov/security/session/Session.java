package com.ilya.ivanov.security.session;

import com.google.common.collect.ImmutableList;
import com.ilya.ivanov.data.model.FileEntity;
import com.ilya.ivanov.data.model.UserEntity;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ilya on 5/20/17.
 */
public class Session {
    private UserEntity userEntity;

    private List<Pair<String, Long>> added = new ArrayList<>();

    private List<Pair<String, Long>> removed = new ArrayList<>();

    private boolean valid;

    public Session(UserEntity userEntity) {
        this.userEntity = userEntity;
        this.valid = true;
    }

    public UserEntity getUserEntity() {
        if (valid)
            return userEntity;
        else
            throw new IllegalStateException("Session is invalid");
    }

    public List<Pair<String, Long>> getAdded() {
        return ImmutableList.copyOf(added);
    }

    public List<Pair<String, Long>> getRemoved() {
        return ImmutableList.copyOf(removed);
    }

    public void newAdded(List<FileEntity> files) {
        List<Pair<String, Long>> collect =
                files.stream()
                        .filter(FileEntity::isFile)
                        .map(f -> new Pair<>(f.getFilename(), f.getFileSize())).collect(Collectors.toList());
        added.addAll(collect);
    }

    public void newRemoved(List<FileEntity> files) {
        List<Pair<String, Long>> collect =
                files.stream()
                        .filter(FileEntity::isFile)
                        .map(f -> new Pair<>(f.getFilename(), f.getFileSize())).collect(Collectors.toList());
        removed.addAll(collect);
    }

    boolean isValid() {
        return valid;
    }

    void invalidate() {
        this.valid = false;
    }
}
