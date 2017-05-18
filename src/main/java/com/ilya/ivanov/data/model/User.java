package com.ilya.ivanov.data.model;

import javax.persistence.*;

/**
 * Created by ilya on 5/18/17.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    // spring boot + javaFX
    // email
    // spring security for standalone app
    // sessions for standalone app
    // bean validation
    // jndi
}
