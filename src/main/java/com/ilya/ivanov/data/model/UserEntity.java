package com.ilya.ivanov.data.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
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
@Component
@Lazy
@Scope("prototype")
public class UserEntity {
    private static final Role DEFAULT = Role.USER;

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_id")
    private FileEntity root;

    private UserEntity() {
    }

    public UserEntity(String email, String password) {
        this(email, password, DEFAULT);
    }

    public UserEntity(String email, String password, Role role) {
        this(email, password, role, FileEntity.createDirectory(null, null,"root"));
    }

    public UserEntity(String email, String password, Role role, FileEntity root) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.root = root;
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

    public FileEntity getRoot() {
        return root;
    }

    public boolean hasReadPermission() {
        return role.hasReadPermission();
    }

    public boolean hasWritePermission() {
        return role.hasWritePermission();
    }

    public boolean hasExecutePermission() {
        return role.hasExecutePermission();
    }

    public boolean hasEditPermission() {
        return role.hasEditPermission();
    }

    public boolean hasAdminPermission() {
        return role.hasAdminPermission();
    }

    public boolean hasRole(Role role) {
        return this.role.hasRole(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserEntity that = (UserEntity) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", root=" + root +
                '}';
    }
}
