package com.ilya.ivanov.service.file;

import java.io.File;

/**
 * Created by ilya on 6/4/17.
 */
public interface DownloadContext extends FileConflictContext {
    File getParent();

    boolean createParentIfNotExists();
}
