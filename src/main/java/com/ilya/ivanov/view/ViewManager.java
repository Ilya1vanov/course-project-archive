package com.ilya.ivanov.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ilya on 5/20/17.
 */
public class ViewManager {
    /* currently shown view */
    private View currentViewName;

    /* mapping viewName -> Stage */
    private final HashMap<String, View> views = new HashMap<>();

    public ViewManager(Map<String, String> views) {
        views.forEach(this::addView);
    }

    public void addView(String name, String path) {
        views.put(name, loadView(name, path));
    }

    public View getView(String stageName) {
        return views.get(stageName);
    }

    public View getCurrentView() {
        return views.get(currentViewName);
    }

    public void hideAllAndShow(String viewName) throws RuntimeException {
        View newView = views.get(viewName);
        if (newView == null)
            throw new RuntimeException("No such view: " + viewName);
        views.values().forEach(view -> view.getView().getScene().getWindow().hide());
        ((Stage)newView.getView().getScene().getWindow()).show();
        currentViewName = newView;
    }

    private View loadView(String name, String url) {
        try (InputStream fxmlStream = getClass().getClassLoader().getResourceAsStream(url)) {
            FXMLLoader loader = new FXMLLoader();
            loader.load(fxmlStream);
            return new View(name, loader.getRoot(), loader.getController());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while instantiating view: " + name, e);
        }
    }

    public class View {
        private String name;
        private Parent view;
        private Object controller;

        public View(String name, Parent view, Object controller) {
            this.name = name;
            this.view = view;
            this.controller = controller;
        }

        public String getName() {
            return name;
        }

        public Parent getView() {
            return view;
        }

        public Object getController() {
            return controller;
        }
    }
}
