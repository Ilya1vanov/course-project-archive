package com.ilya.ivanov.controller;

import com.ilya.ivanov.security.authentication.AuthenticationService;
import com.ilya.ivanov.security.registration.RegistrationService;
import com.ilya.ivanov.data.model.UserDto;
import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.security.session.SessionManager;
import com.ilya.ivanov.view.ViewManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;

/**
 * Created by ilya on 5/20/17.
 */
public class LoginController {
    public GridPane loginPane;
    public TextField emailField;
    public Text warningText;
    public PasswordField passwordField;
    public GridPane registrationPane;
    public PasswordField confirmPasswordField;
    public Button signInBtn;
    public Button signUpBtn;

    @Autowired private ViewManager viewManager;

    @Autowired private AuthenticationService authenticationService;

    @Autowired private RegistrationService registrationService;

    @Autowired private Validator validator;

    @Autowired private SessionManager sessionManager;

    public void initialize() {
        BooleanBinding or = Bindings.or(emailField.textProperty().isEmpty(), passwordField.textProperty().isEmpty());
        signUpBtn.disableProperty().bind(Bindings.or(or, confirmPasswordField.textProperty().isEmpty()));
        signInBtn.disableProperty().bind(or);
    }

    public void handleRegistrationSwitch() {
        loginPane.setVisible(false);
        registrationPane.setVisible(true);
    }

    public void handleLoginSwitch() {
        registrationPane.setVisible(false);
        loginPane.setVisible(true);
    }

    public void handleSignUp() throws Exception {
        UserDto userDto =
                new UserDto(emailField.getText(), passwordField.getText(), confirmPasswordField.getText());
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        boolean rejectedEmail = false;
        if (violations.isEmpty()) {
            Optional<UserEntity> register = registrationService.register(userDto);
            if (register.isPresent()) {
                handleSignIn();
                return;
            }
            else {
                rejectedEmail = true;
            }
        }
        if (!violations.isEmpty() || rejectedEmail) {
            String text = rejectedEmail ? "Email exists" : violations.iterator().next().getMessage();
            warningText.setText(text);
            warningText.setVisible(true);
        }
    }

    public void handleSignIn() {
        String email = emailField.getText();
        String password = passwordField.getText();
        Optional<UserEntity> authentication = authenticationService.authenticate(email, password);
        if (authentication.isPresent()) {
            UserEntity userEntity = authentication.get();
            sessionManager.newSession(userEntity);
            this.clearFields();
            viewManager.hideAllAndShow("mainView");
        }
        else {
            warningText.setText("Wrong email or password!");
            warningText.setVisible(true);
            passwordField.requestFocus();
        }
    }

    private void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    public void handleFieldChanged() {
        warningText.setVisible(false);
    }
}
