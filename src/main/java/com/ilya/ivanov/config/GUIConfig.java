package com.ilya.ivanov.config;

import com.ilya.ivanov.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ilya on 5/20/17.
 */
@Configuration
public class GUIConfig {
    @Bean(name = "mainView")
    @Lazy
    public View getMainView(@Value("${ui.views.main}") String path) throws IOException {
        return loadView(path);
    }

    @Bean(name = "mainController")
    @Lazy
    public MainController getMainController(View mainView) throws IOException {
        return (MainController) mainView.getController();
    }

    @Bean(name = "loginView")
    @Lazy
    public View getLoginView(@Value("${ui.views.login}") String path) throws IOException {
        return loadView(path);
    }

    @Bean(name = "mainController")
    @Lazy
    public MainController getLoginController(View loginView) throws IOException {
        return (MainController) loginView.getController();
    }

    private View loadView(String url) throws IOException {
        InputStream fxmlStream = null;
        try {
            fxmlStream = getClass().getClassLoader().getResourceAsStream(url);
            FXMLLoader loader = new FXMLLoader();
            loader.load(fxmlStream);
            return new View(loader.getRoot(), loader.getController());
        } finally {
            if (fxmlStream != null) {
                fxmlStream.close();
            }
        }
    }

    public class View {
        private Parent view;
        private Object controller;

        public View(Parent view, Object controller) {
            this.view = view;
            this.controller = controller;
        }

        public Parent getView() {
            return view;
        }

        public Object getController() {
            return controller;
        }
    }
}
