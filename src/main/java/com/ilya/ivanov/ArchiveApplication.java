package com.ilya.ivanov;

import com.ilya.ivanov.controller.MainController;
import com.ilya.ivanov.data.model.file.FileEntity;
import com.ilya.ivanov.data.model.user.Role;
import com.ilya.ivanov.data.model.user.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import com.ilya.ivanov.security.session.SessionManager;
import com.ilya.ivanov.view.AbstractJavaFxApplicationSupport;
import com.ilya.ivanov.view.ViewManager;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.measure.quantity.DataAmount;
import javax.measure.unit.CompoundUnit;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Objects;

@SpringBootApplication
@Component
@ImportResource({"classpath:gui-context.xml", "classpath:aspect-context.xml", "classpath:stylesheets-context.xml"})
public class ArchiveApplication extends AbstractJavaFxApplicationSupport {
	private static final Logger log = Logger.getLogger(ArchiveApplication.class);

    @Autowired private ViewManager viewManager;

    @Autowired private SessionManager sessionManager;

    @Autowired private Environment env;

    public static void main(String[] args) {
        launchApp(ArchiveApplication.class, args);
	}

    @Override
    public void start(Stage primaryStage) throws Exception {
        Objects.requireNonNull(viewManager, "View manager cannot be null");
        Stage mainStage = primaryStage;
        mainStage.setTitle(env.getProperty("ui.views.main.title"));
        mainStage.setMinWidth(env.getProperty("ui.views.main.minWidth", Double.class));
        mainStage.setMinHeight(env.getProperty("ui.views.main.minHeight", Double.class));
        mainStage.setResizable(env.getProperty("ui.views.main.resizable", Boolean.class));
        Parent mainView = viewManager.getView("mainView").getView();
        mainStage.setScene(new Scene(mainView));

        Stage loginStage = new Stage();
        loginStage.setTitle(env.getProperty("ui.views.login.title"));
        loginStage.setMinWidth(env.getProperty("ui.views.login.minWidth", Double.class));
        loginStage.setMinHeight(env.getProperty("ui.views.login.minHeight", Double.class));
        loginStage.setResizable(env.getProperty("ui.views.login.resizable", Boolean.class));
        Parent loginView = viewManager.getView("loginView").getView();
        loginStage.setScene(new Scene(loginView));

        ((MainController)viewManager.getView("mainView").getController()).initAfterDI();
        viewManager.hideAllAndShow("loginView");
        mainStage.setOnCloseRequest((e) -> sessionManager.invalidateSession());
    }

	@Bean
    @Profile("dev")
    public CommandLineRunner development(UserRepository repository, PasswordEncoder encoder, Validator validator) {
	    return (args) -> {
            addAdmin(repository, encoder);
        };
    }

    @Bean
    @Profile("prod")
    public CommandLineRunner production(UserRepository repository, PasswordEncoder encoder) {
        return (args) -> addAdmin(repository, encoder);
    }

    private void addAdmin(UserRepository repository, PasswordEncoder encoder) throws IOException {
        String password = encoder.encode("ilya");
        UserEntity admin = new UserEntity("com.ilya.ivanov@gmail.com", password, Role.ADMIN);
        admin.getRoot().createDirectory(null, "dir2");
        final FileEntity dir1 = admin.getRoot().createDirectory(null, "dir1");
        for (int i = 0; i < 40; i++) {
            dir1.createFile("file-" + i, new byte[]{2, 3, 4});
        }
        if (!repository.exists(Example.of(new UserEntity(admin.getEmail(), null, null, null))))
            repository.save(admin);
    }
}
