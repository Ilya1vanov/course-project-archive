package com.ilya.ivanov.controller;

import com.ilya.ivanov.data.model.user.UserDto;
import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.security.session.NewSessionEvent;
import com.ilya.ivanov.security.session.SessionManager;
import com.ilya.ivanov.service.user.UserService;
import com.ilya.ivanov.view.ViewManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;

/**
 * Created by ilya on 5/20/17.
 */
public class LoginController implements ApplicationListener<NewSessionEvent> {
    public GridPane loginPane;
    public TextField emailField;
    public Text warningText;
    public PasswordField passwordField;
    public GridPane registrationPane;
    public PasswordField confirmPasswordField;
    public Button signInBtn;
    public Button signUpBtn;

    @Autowired private ViewManager viewManager;

    @Autowired private UserService userService;

    @Autowired private Validator validator;

    @Autowired private SessionManager sessionManager;

    public void initialize() {
        BooleanBinding or = Bindings.or(emailField.textProperty().isEmpty(), passwordField.textProperty().isEmpty());
        signUpBtn.disableProperty().bind(Bindings.or(or, confirmPasswordField.textProperty().isEmpty()));
        signInBtn.disableProperty().bind(or);
        loginPane.visibleProperty().bind(registrationPane.visibleProperty().not());
        TextFields.bindAutoCompletion(emailField, param -> userService.getAutocompletion(param.getUserText()));
    }

    public void handleRegistrationSwitch() {
        registrationPane.setVisible(true);
    }

    public void handleLoginSwitch() {
        registrationPane.setVisible(false);
    }

    public void handleSignUp() throws Exception {
        UserDto userDto = getDto();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        boolean rejectedEmail = false;
        if (violations.isEmpty()) {
            Optional<UserEntity> register = userService.register(userDto);
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

    private UserDto getDto() {
        return new UserDto(emailField.getText(), passwordField.getText(), confirmPasswordField.getText());
    }

    public void handleSignIn() {
        String email = emailField.getText();
        String password = passwordField.getText();
        Optional<UserEntity> authentication = userService.authenticate(email, password);
        if (authentication.isPresent()) {
            UserEntity userEntity = authentication.get();
            sessionManager.newSession(userEntity);
            viewManager.hideAllAndShow("mainView");
        }
        else {
            warningText.setText("Wrong email or password!");
            warningText.setVisible(true);
            passwordField.requestFocus();
        }
    }

    public void handleFieldChanged() {
        warningText.setVisible(false);
    }

    @Override
    public void onApplicationEvent(NewSessionEvent newSessionEvent) {
        this.clearFields();
        passwordField.requestFocus();
    }

    private void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
}
