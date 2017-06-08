package com.ilya.ivanov.service.file;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by ilya on 6/4/17.
 */
public class SimpleDownloadContext extends SimpleFIleConflictContext implements DownloadContext {
    private File file;

    private boolean createParentIfNotExists;

    private SimpleDownloadContext(File file, boolean replaceExisting, boolean createParentIfNotExists) {
        super(replaceExisting);
        this.file = file;
        this.createParentIfNotExists = createParentIfNotExists;
    }

    public static SimpleDownloadContext create(File file, boolean replaceExisting, boolean createParentIfNotExists) {
        return new SimpleDownloadContext(file, replaceExisting, createParentIfNotExists);
    }

    @Override
    public File getParent() {
        return file;
    }

    @Override
    public boolean createParentIfNotExists() {
        return createParentIfNotExists;
    }
}
