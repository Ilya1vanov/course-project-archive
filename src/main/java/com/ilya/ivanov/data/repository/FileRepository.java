package com.ilya.ivanov.data.repository;

import com.ilya.ivanov.data.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by ilya on 5/19/17.
 */
public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
