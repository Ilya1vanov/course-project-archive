package com.ilya.ivanov.data.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.*;

/**
 * Created by ilya on 5/18/17.
 */
@Entity
@Table(name = "users")
//@Configurable(preConstruction = true, dependencyCheck = true)
public class UserEntity {
    public static final Role DEFAULT = Role.USER;

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

    @OneToOne
    @JoinColumn(name = "root_id")
    private FileEntity root;

    private UserEntity() {
    }

    public UserEntity(String email, String password) {
        this(email, password, DEFAULT);
    }

    public UserEntity(String email, String password, Role role) {
        this.email = email;
        this.setPassword(password);
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
