package com.ilya.ivanov.data.repository;

import com.ilya.ivanov.data.model.file.FileEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Created by ilya on 5/19/17.
 */
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    long countByFilenameContaining(String query);

    Slice<FileEntity> findByFilenameContaining(String filename, Pageable pageable);
}
