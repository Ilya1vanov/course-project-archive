package com.ilya.ivanov.view;

import javafx.scene.Scene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by ilya on 6/1/17.
 */
@Component
public class CSSDriver {
    private Style current;

    private List<Style> stylesheets = new ArrayList<>();

    @Autowired
    public CSSDriver(@Qualifier("stylesheets") TreeMap<String, String> stylesheets) {
        stylesheets.forEach((s, s2) -> this.stylesheets.add(new Style(s, s2)));
        if (!stylesheets.isEmpty())
            this.current = this.stylesheets.iterator().next();
    }

    public static class Style {
        private String name;
        private String path;

        public Style(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }

    public void setNext(Scene scene) {
        if (scene.getStylesheets().contains(current.getPath()))
            scene.getStylesheets().remove(current.getPath());
        ListIterator<Style> iterator = stylesheets.listIterator(stylesheets.indexOf(current));
        iterator.next();
        if (!iterator.hasNext())
            iterator = stylesheets.listIterator();
        current = iterator.next();
        scene.getStylesheets().add(current.getPath());
    }

    public List<Style> getStylesheets() {
        return stylesheets;
    }
}
