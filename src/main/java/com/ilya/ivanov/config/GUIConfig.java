package com.ilya.ivanov.config;

import com.ilya.ivanov.controller.LoginController;
import com.ilya.ivanov.controller.MainController;
import com.ilya.ivanov.view.ViewManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Created by ilya on 5/21/17.
 */
@Configuration
@ComponentScan(basePackages = "com.ilya.ivanov.view")
@DependsOn("viewManager")
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
