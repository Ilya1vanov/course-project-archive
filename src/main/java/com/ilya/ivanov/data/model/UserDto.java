package com.ilya.ivanov.data.model;

import com.ilya.ivanov.data.constraints.annotation.PasswordMatches;
import com.ilya.ivanov.data.constraints.annotation.ValidEmail;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by ilya on 5/19/17.
 */
@PasswordMatches
public class UserDto {
    @NotNull
    @ValidEmail
    private String email;

    @NotNull
    @Size(min = 5, max = 25)
    private String password;

    private String matchingPassword;

    private UserDto() {
    }

    public UserDto(String email, String password, String matchingPassword) {
        this.email = email;
        this.password = password;
        this.matchingPassword = matchingPassword;
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

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }
}
