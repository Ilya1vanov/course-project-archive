package com.ilya.ivanov.service.file;

import com.ilya.ivanov.data.model.file.FileEntity;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by ilya on 6/3/17.
 */
public interface FileService {
    FileEntity renameFile(FileEntity fileEntity, String filename);

    void removeFilesById(List<Long> ids);

    void removeFiles(List<FileEntity> files);

    Collection<FileEntity> addDirectory(FileEntity parent, String filename);

    Collection<FileEntity> addFiles(FileEntity parent, List<File> source);

    List<Future<Object>> openFile(FileEntity fileEntity) throws IOException, InterruptedException;

    Collection<File> downloadFiles(File parent, List<FileEntity> files);
}
