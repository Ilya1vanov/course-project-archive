package com.ilya.ivanov.service.file;

import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.security.session.InvalidateSessionEvent;
import com.ilya.ivanov.security.session.NewSessionEvent;
import javafx.concurrent.Task;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by ilya on 6/3/17.
 */
public interface FileService extends ApplicationListener<InvalidateSessionEvent> {
    Task<FileEntity> renameFile(FileEntity fileEntity, String filename);

    Task<Collection<FileEntity>> removeFiles(Collection<FileEntity> files);

    Task<Collection<FileEntity>> addDirectory(FileEntity parent, String filename);

    Task<Collection<FileEntity>> addFiles(FileEntity parent, List<File> source);

    Task<Void> openFile(FileEntity fileEntity);

    Task<Collection<File>> downloadFiles(DownloadContext context, List<FileEntity> files);
}
