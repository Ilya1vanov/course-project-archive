package com.ilya.ivanov.service.file;

/**
 * Created by ilya on 6/5/17.
 */
public interface FileConflictContext {
    boolean replaceExisting();

    boolean renameIfExist();

    String rename(String filename);
}
