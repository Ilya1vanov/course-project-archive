package com.ilya.ivanov.data.model.file;

import java.io.File;
import java.util.Collection;

/**
 * Created by ilya on 5/31/17.
 */
public class FileDto {
    private FileEntity parent;

    private Collection<File> sources;

    public FileDto(FileEntity parent, Collection<File> sources) {
        this.parent = parent;
        this.sources = sources;
    }

    public FileEntity getParent() {
        return parent;
    }

    public Collection<File> getSources() {
        return sources;
    }
}
