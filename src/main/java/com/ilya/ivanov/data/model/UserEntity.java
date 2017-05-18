package com.ilya.ivanov.data.model;

import javax.persistence.*;

/**
 * Created by ilya on 5/18/17.
 */
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated
    @Column(name = "role")
    private Role role;
}
