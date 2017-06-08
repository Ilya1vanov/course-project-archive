package com.ilya.ivanov.service.file;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.repository.FileRepository;
import com.ilya.ivanov.security.session.InvalidateSessionEvent;
import com.ilya.ivanov.view.DesktopManager;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by ilya on 5/31/17.
 */
@Component
public class DefaultFileService implements FileService {
    private final FileRepository fileRepository;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    public DefaultFileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        System.setProperty("java.awt.headless", "false");
    }

    @Override
    public Task<FileEntity> renameFile(FileEntity fileEntity, String filename) {
        final Task<FileEntity> task = createRenameFileTask(fileEntity, filename);
        task.run();
        return task;
    }

    private Task<FileEntity> createRenameFileTask(FileEntity fileEntity, String filename) {
        return new Task<FileEntity>() {
            @Override
            protected FileEntity call() throws Exception {
                fileEntity.setFilename(filename);
                return fileRepository.save(fileEntity);
            }
        };
    }

    @Override
    public Task<Collection<FileEntity>> removeFiles(Collection<FileEntity> files) {
        final Task<Collection<FileEntity>> task = this.createRemoveFilesTask(files);
        task.run();
        return task;
    }

    private Task<Collection<FileEntity>> createRemoveFilesTask(Collection<FileEntity> files) {
        return new Task<Collection<FileEntity>>() {
            @Override
            protected Collection<FileEntity> call() throws Exception {
                final List<Long> ids = files.stream().map(FileEntity::getId).collect(Collectors.toList());
                for (int i = 0; i < ids.size(); i++) {
                    Long id = ids.get(i);
                    if (fileRepository.exists(id)) {
                        fileRepository.delete(id);
                        updateProgress(i + 1, ids.size());
                    }
                }
                this.updateValue(files);
                this.done();
                return files;
            }
        };
    }

    @Override
    public Task<Collection<FileEntity>> addDirectory(FileEntity parent, String filename) {
        final Task<Collection<FileEntity>> task = createAddDirectoryTask(parent, filename);
        task.run();
        return task;
    }

    private Task<Collection<FileEntity>> createAddDirectoryTask(FileEntity parent, String filename) {
        return new Task<Collection<FileEntity>>() {
            @Override
            protected Collection<FileEntity> call() throws Exception {
                final FileEntity directory = parent.createDirectory(filename);
                final Collection<FileEntity> fileEntities = Lists.newArrayList(fileRepository.save(directory));
                this.updateValue(fileEntities);
                this.done();
                return fileEntities;
            }
        };
    }

    @Override
    public Task<Collection<FileEntity>> addFiles(FileEntity parent, List<File> source) {
        Task<Collection<FileEntity>> task = createAddFilesTask(parent, source);
        executor.submit(task);
        return task;
    }

    private Task<Collection<FileEntity>> createAddFilesTask(FileEntity parent, List<File> source) {
        return new Task<Collection<FileEntity>>() {
            @Override
            protected Collection<FileEntity> call() throws Exception {
                Collection<FileEntity> files = new ArrayList<>();
                for (int i = 0; i < source.size(); i++) {
                    final File file = source.get(i);
                    updateMessage("In process: " + file.getName());
                    updateProgress(i + 1, source.size());
                    files.add(fileRepository.save(parent.createFile(file)));
                }
                this.updateValue(files);
                this.done();
                return files;
            }
        };
    }

    @Override
    public Task<Void> openFile(FileEntity fileEntity) {
        final File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileEntity.getFilename());
        final Task<Void> openTask = createOpenFileTask(fileEntity, file);
        executor.submit(openTask);
        return openTask;
    }

    private Task<Void> createOpenFileTask(FileEntity fileEntity, File file) {
        return new CustomTask<Void>() {
            @Override
            protected Void call() throws Exception {
                final List<Callable<?>> tasks = createOpenTasks(fileEntity, file);
                for (int i = 0; i < tasks.size(); i++) {
                    tasks.get(i).call();
                    updateProgress(i + 1, tasks.size());
                }
                return null;
            }

            private List<Callable<?>> createOpenTasks(FileEntity fileEntity, File file) {
                List<Callable<?>> tasks = new ArrayList<>(2);
                if (!file.exists()) {
                    file.deleteOnExit();
                    tasks.add(createFileInstanceTask(fileEntity, file, SimpleFIleConflictContext.create(true)));
                }
                tasks.add(createDesktopOpenTask(file));
                return tasks;
            }

            private Callable<Void> createDesktopOpenTask(File file) {
                return DesktopManager.open(file);
            }
        };
    }

    @Override
    public Task<Collection<File>> downloadFiles(DownloadContext context, List<FileEntity> files) {
        final Task<Collection<File>> downloadTask = createDownloadFilesTask(context, files);
        executor.submit(downloadTask);
        return downloadTask;
    }

    private Task<Collection<File>> createDownloadFilesTask(DownloadContext context, List<FileEntity> files) {
        return new CustomTask<Collection<File>>() {
            @Override
            protected Collection<File> call() throws Exception {
                final File parent = context.getParent();
                if (!createParent(parent))
                    return null;
                List<File> instances = new ArrayList<>(files.size());
                for (int i = 0; i < files.size(); i++) {
                    FileEntity file = files.get(i);
                    final File instance = createFileInstanceTask(file, new File(parent.getPath() + "/" + file.getFilename()), context).call();
                    instances.add(instance);
                    updateProgress(i + 1, files.size());
                }
                return instances;
            }

            private boolean createParent(File parent) throws IOException {
                if (!parent.exists())
                    if (context.createParentIfNotExists())
                        return parent.mkdirs();
                    else {
                        this.cancel(true);
                        return false;
                    }
                else return true;
            }
        };
    }

    private static abstract class CustomTask<V> extends Task<V> {
        Callable<File> createFileInstanceTask(FileEntity fileEntity, File file, FileConflictContext context) {
            return () -> {
                if (file.exists())
                    if (!context.replaceExisting()) {
                        return null;
                    }
                file.createNewFile();
                final byte[] data = fileEntity.getFile();
                try (InputStream fis = new ByteInputStream(data, data.length);
                     FileOutputStream fos = new FileOutputStream(file)) {
                    ByteStreams.copy(fis, fos);
                }
                return file;
            };
        }
    }

    @Override
    public void onApplicationEvent(InvalidateSessionEvent invalidateSessionEvent) {
        // do some reset stuff
    }
}
