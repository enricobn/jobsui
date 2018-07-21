package org.jobsui.core.ui;

import javafx.scene.Scene;

import java.net.URL;

/**
 * Created by enrico on 4/13/17.
 */
public enum JobsUITheme {
    Standard() {
        @Override
        public void applyToScene(Scene scene) {
            scene.getStylesheets().addAll(
                resourceToURL("/standard.css"),
                resourceToURL("/shared.css"));
        }
    },
    Material {
        @Override
        public void applyToScene(Scene scene) {
            scene.getStylesheets().addAll(
                resourceToURL("/resources/css/jfoenix-fonts.css"),
                resourceToURL("/resources/css/jfoenix-design.css"),
                resourceToURL("/material.css"),
                resourceToURL("/shared.css")
            );
        }
    },
    Dark {
        @Override
        public void applyToScene(Scene scene) {
            scene.getStylesheets().addAll(
                resourceToURL("/dark.css"),
                resourceToURL("/shared.css"));
        }
    };

    public abstract void applyToScene(Scene scene);

    private static String resourceToURL(String resource) {
        URL url = JobsUITheme.class.getResource(resource);
        return url.toExternalForm();
    }

}
