package com.ilya.ivanov.data.model;

import com.ilya.ivanov.config.AppConfig;
import com.ilya.ivanov.config.AspectConfig;
import com.ilya.ivanov.config.JpaConfig;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by ilya on 5/20/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {JpaConfig.class, AspectConfig.class})
// VM options: -ea
public class FileEntityTest {
    @Test
    public void noAssertionsOnDirectoryCreation() throws Exception {
        FileEntity.createDirectory(null, null, "root");
    }

    @Test
    public void noAssertionsOnFileCreation() throws Exception {
        FileEntity.createFile(null, "dir1",  null);
    }

    @Test(expected = BeanCreationException.class)
    public void assertionOnEmptyFilename() throws Exception {
        FileEntity.createFile(null, "",  null);
    }

    @Test
    public void shouldWireChildrenRight() throws Exception {
        FileEntity root = FileEntity.createDirectory(null, null, "root");
        FileEntity file = FileEntity.createFile(root, "name", null);

        assertThat(root.getChildren(), notNullValue());
        Assertions.assertThat(root.getChildren()).contains(file);
        assertThat(file, is(root.getChildren().get(0)));
        assertThat(file.getFileSize(), notNullValue());
    }

    @Test
    public void directoryGetFileShouldReturnNull() throws Exception {
        FileEntity root = FileEntity.createDirectory(null, null, "root");
        byte[] file = root.getFile();

        assertThat(file, nullValue());
    }
}