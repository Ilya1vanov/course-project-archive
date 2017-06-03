package com.ilya.ivanov.service.file;

import com.google.common.collect.Lists;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.repository.FileRepository;
import com.ilya.ivanov.model.DirectoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by ilya on 5/31/17.
 */
@Component
public class FileService {
    private final FileRepository fileRepository;

    private final DirectoryManager directoryManager;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    public FileService(FileRepository fileRepository, DirectoryManager directoryManager) {
        this.fileRepository = fileRepository;
        this.directoryManager = directoryManager;
    }

    public FileEntity renameFile(FileEntity fileEntity, String filename) {
        fileEntity.setFilename(filename);
        fileEntity.touch();
        return fileRepository.save(fileEntity.getParent());
    }

    public void removeFiles(List<FileEntity> files) {
        files.forEach((f) -> fileRepository.delete(f.getId()));
    }

    public Collection<FileEntity> addDirectory(FileEntity parent, String filename) {
        final FileEntity directory = parent.createDirectory(filename);
        return Lists.newArrayList(fileRepository.save(directory));
    }

    public Collection<FileEntity> addFiles(FileEntity parent, List<File> source) {
        Collection<Callable<FileEntity>> tasks = createTasks(parent, source);
        return executeSave(tasks, source.size());
    }

    public Collection<File> downloadFiles(List<FileEntity> files) {
        return null;
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

    private Collection<Callable<FileEntity>> createTasks(FileEntity parent, List<File> source) {
        Collection<Callable<FileEntity>> tasks = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            final File file = source.get(i);
            tasks.add(() -> fileRepository.save(parent.createFile(file)));
        }
        return tasks;
    }
}
