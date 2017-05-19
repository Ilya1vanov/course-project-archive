package com.ilya.ivanov.data.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.ilya.ivanov.config.AppConfig;
import com.ilya.ivanov.config.JpaConfig;
import com.ilya.ivanov.data.model.UserEntity;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 * Created by ilya on 5/19/17.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JpaConfig.class, AppConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:META-INF/database.xml")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindAll() {
        final List<UserEntity> all = userRepository.findAll();
        Assertions.assertThat(all).isNotNull().isNotEmpty();
    }

    @Test
    public void testFindOne() {
        final UserEntity one = userRepository.getOne(2L);
        assertThat("Cannot find record by ID", one.getEmail(), is("ilya@gmail.com"));
    }

    @Test
    public void testSave() {
        UserEntity user = new UserEntity("ololo", "1234");
        UserEntity savedUser = userRepository.save(user);

        UserEntity userFromDb = userRepository.findOne(savedUser.getId());

        assertThat(user, is(savedUser));
        assertThat(userFromDb.getId(), is(user.getId()));
    }

    @Test
    public void testDelete() {
        UserEntity user = new UserEntity("trololo", "12345");

        userRepository.save(user);
        UserEntity userFromDb = userRepository.findOne(user.getId());
        userRepository.delete(userFromDb);

        assertNull(userRepository.findOne(userFromDb.getId()));
    }

    @Test
    public void testFindByLoginAndPassword() {
        final UserEntity entity = userRepository.findByEmailAndPassword("ilya@gmail.com", "ilya");
        assertThat(entity, notNullValue());
        assertThat(entity.getEmail(), is("ilya@gmail.com"));
        assertThat(entity.getPassword(), is("ilya"));
    }

    @Test
    public void testCountByLogin() {
        final boolean ilya = userRepository.exists(Example.of(new UserEntity("ilya", null, null)));
        final boolean nonexistent = userRepository.exists(Example.of(new UserEntity("nonexistent login", null, null)));
        assertThat(ilya, is(true));
        assertThat(nonexistent, is(false));
    }

}