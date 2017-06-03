package com.ilya.ivanov.data.repository;

import com.ilya.ivanov.config.JpaConfig;
import com.ilya.ivanov.data.model.file.FileEntity;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by ilya on 5/19/17.
 */
@ActiveProfiles(profiles = "dev")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JpaConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
public class FileRepositoryTest {
    @Autowired
    private FileRepository fileRepository;

    private FileEntity root = FileEntity.createDirectory(null, null ,"root");

    private FileEntity file1;

    private FileEntity file2;

    private Long id;

    @Before
    public void setUp() throws Exception {
        file1 = FileEntity.createFile(root, "file1", null);
        file2 = FileEntity.createFile(root, "file2", new byte[] {1, 2, 3, 4, 5, 6});
        id = fileRepository.save(root).getId();
        fileRepository.save(file1);
        fileRepository.save(file2);
    }

    @Test
    public void testFindAll() {
        final List<FileEntity> all = fileRepository.findAll();
        Assertions.assertThat(all).isNotNull().isNotEmpty();
        Assertions.assertThat(all).contains(file1, file2);
    }

    @Test
    public void testFindOne() {
        final FileEntity one = fileRepository.findOne(id);
        Assertions.assertThat(one.getChildren()).contains(file1, file2);
        assertThat("Cannot find record by ID", one.getFilename(), is("root"));
    }

    @Test
    public void testSave() {
        FileEntity user = FileEntity.createDirectory(null, null, "dir2");
        FileEntity savedUser = fileRepository.save(user);

        FileEntity userFromDb = fileRepository.findOne(savedUser.getId());

        assertThat(user, is(savedUser));
        assertThat(userFromDb.getId(), is(user.getId()));
    }

    @Test
    public void testDelete() {
        FileEntity user = FileEntity.createDirectory(null, null, "dir3");

        fileRepository.save(user);
        FileEntity userFromDb = fileRepository.findOne(user.getId());
        fileRepository.delete(userFromDb);

        assertNull(fileRepository.findOne(userFromDb.getId()));
    }

    @Test
    public void deleteShouldBeCascade() {
        List<FileEntity> allBefore = fileRepository.findAll();
        fileRepository.delete(root);

        List<FileEntity> all = fileRepository.findAll();
        Assertions.assertThat(allBefore).isNotEmpty().hasSize(3);
        Assertions.assertThat(all).isEmpty();
    }
}