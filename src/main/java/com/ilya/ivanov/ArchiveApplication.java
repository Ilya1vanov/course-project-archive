package com.ilya.ivanov;

import com.ilya.ivanov.data.model.Role;
import com.ilya.ivanov.data.model.UserDto;
import com.ilya.ivanov.data.model.UserEntity;
import com.ilya.ivanov.data.repository.UserRepository;
import com.ilya.ivanov.view.AbstractJavaFxApplicationSupport;
import com.ilya.ivanov.view.ViewManager;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@SpringBootApplication
@Component
@ImportResource("classpath:gui-context.xml")
//@EntityScan( basePackages = {"com.ilya.ivanov.data.model"} )
public class ArchiveApplication extends AbstractJavaFxApplicationSupport {
	private static final Logger log = Logger.getLogger(ArchiveApplication.class);

    @Autowired private ViewManager viewManager;

    @Autowired private Environment env;

    public static void main(String[] args) {
        launchApp(ArchiveApplication.class, args);
	}

    @Override
    public void start(Stage primaryStage) throws Exception {
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

        viewManager.hideAllAndShow("loginView");
    }

	@Bean
    @Profile("dev")
    public CommandLineRunner development(UserRepository repository, PasswordEncoder encoder, Validator validator) {
	    return (args) -> {
            addAdmin(repository, encoder);
			UserDto dto = new UserDto("asd", "a", "v");
            DataBinder dataBinder = new DataBinder(dto);
//            dataBinder.bind();
            LocalValidatorFactoryBean localValidator = new LocalValidatorFactoryBean();

            dataBinder.setValidator(localValidator);
            dataBinder.validate();
            BindingResult bindingResult = dataBinder.getBindingResult();
		};
    }

    @Bean
    @Profile("prod")
    public CommandLineRunner production(UserRepository repository, PasswordEncoder encoder) {
        return (args) -> addAdmin(repository, encoder);
    }

    private void addAdmin(UserRepository repository, PasswordEncoder encoder) {
        String password = encoder.encode("ilya");
        UserEntity admin = new UserEntity("com.ilya.ivanov@gmail.com", password, Role.ADMIN);
        if (!repository.exists(Example.of(new UserEntity(admin.getEmail(), null, null, null))))
            repository.save(admin);
    }
}
