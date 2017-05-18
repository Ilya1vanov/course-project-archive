package com.ilya.ivanov.data.repository;

import com.ilya.ivanov.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by ilya on 5/18/17.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}