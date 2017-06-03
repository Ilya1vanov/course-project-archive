package com.ilya.ivanov.data.repository;

import com.ilya.ivanov.data.model.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * Created by ilya on 5/18/17.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmailAndPassword(String email, String password);

    UserEntity findByEmail(String email);

    Collection<UserEntity> findByEmailStartsWith(String email);
}
