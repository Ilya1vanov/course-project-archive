package com.ilya.ivanov.config;

import com.ilya.ivanov.controller.LoginController;
import com.ilya.ivanov.controller.MainController;
import com.ilya.ivanov.view.CSSDriver;
import com.ilya.ivanov.view.ViewManager;
import javafx.application.Platform;
import javafx.stage.Stage;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ilya on 5/21/17.
 */
@Configuration
@ComponentScan(basePackages = "com.ilya.ivanov.view")
public class GUIConfig {
    private final ViewManager viewManager;

    public GUIConfig(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    @Bean
    public LoginController loginController() {
        return (LoginController) viewManager.getView("loginView").getController();
    }

    @Bean
    public MainController mainController() {
        return (MainController) viewManager.getView("mainView").getController();
    }
}
