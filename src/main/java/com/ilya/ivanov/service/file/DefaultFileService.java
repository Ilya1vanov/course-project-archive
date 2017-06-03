package com.ilya.ivanov.service.file;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.repository.FileRepository;
import com.ilya.ivanov.service.DirectoryManager;
import com.ilya.ivanov.view.DesktopBrowser;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by ilya on 5/31/17.
 */
@Component
public class DefaultFileService implements FileService {
    private final FileRepository fileRepository;

    private final DirectoryManager directoryManager;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    public DefaultFileService(FileRepository fileRepository, DirectoryManager directoryManager) {
        this.fileRepository = fileRepository;
        this.directoryManager = directoryManager;
    }

    public FileEntity renameFile(FileEntity fileEntity, String filename) {
        fileEntity.setFilename(filename);
        fileEntity.touch();
        return fileRepository.save(fileEntity.getParent());
    }

    @Override
    public void removeFilesById(List<Long> ids) {
        ids.forEach(fileRepository::delete);
    }

    @Override
    public void removeFiles(List<FileEntity> files) {
        this.removeFilesById(files.stream().map(FileEntity::getId).collect(Collectors.toList()));
    }

    @Override
    public Collection<FileEntity> addDirectory(FileEntity parent, String filename) {
        final FileEntity directory = parent.createDirectory(filename);
        return Lists.newArrayList(fileRepository.save(directory));
    }

    @Override
    public Collection<FileEntity> addFiles(FileEntity parent, List<File> source) {
        Collection<Callable<FileEntity>> tasks = createAddTasks(parent, source);
        return executeSave(tasks, source.size());
    }

    private Collection<FileEntity> executeSave(Collection<Callable<FileEntity>> tasks, int size) {
        Collection<FileEntity> files = new ArrayList<>(size);
        try {
            final List<Future<FileEntity>> futures = executor.invokeAll(tasks);
            for (Future<FileEntity> future : futures) {
                files.add(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO collaborate with
            e.printStackTrace();
        }
        Runtime.getRuntime().gc();
        return files;
    }

    private Collection<Callable<FileEntity>> createAddTasks(FileEntity parent, List<File> source) {
        Collection<Callable<FileEntity>> tasks = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            final File file = source.get(i);
            tasks.add(() -> fileRepository.save(parent.createFile(file)));
        }
        return tasks;
    }

    @Override
    public List<Future<Object>> openFile(FileEntity fileEntity) throws IOException, InterruptedException {
        final File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileEntity.getFilename());
        final Collection<Callable<Object>> tasks = createOpenTasks(fileEntity, file);
        return executor.invokeAll(tasks);
    }

    private Collection<Callable<Object>> createOpenTasks(FileEntity fileEntity, File file) throws IOException {
        Collection<Callable<Object>> tasks = new ArrayList<>(2);
        if (!file.exists()) {
            if (!file.createNewFile())
                throw new IOException("Cannot create temp file");
            file.deleteOnExit();
            tasks.add(() -> {
                final byte[] data = fileEntity.getFile();
                try (InputStream fis = new ByteInputStream(data, data.length);
                     FileOutputStream fos = new FileOutputStream(file)) {
                    ByteStreams.copy(fis, fos);
                }
                return null;
            });
        }
        tasks.add(() -> {new DesktopBrowser(file).run(); return null;});
        return tasks;
    }

    @Override
    public Collection<File> downloadFiles(File parent, List<FileEntity> files) {
        return null;
    }
}
