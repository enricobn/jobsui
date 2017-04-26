/*
* Copyright (c) 2008, 2011 Oracle and/or its affiliates.
* All rights reserved. Use is subject to license terms.
*
* This file is available and licensed under the following license:
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
*  - Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*  - Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the distribution.
*  - Neither the name of Oracle Corporation nor the names of its
*    contributors may be used to endorse or promote products derived
*    from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
* A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
* OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
* DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
* THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.jobsui.ui.javafx;

import com.jfoenix.controls.JFXDecorator;
import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.ui.UI;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.edit.EditProject;

import java.io.Serializable;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Application. This class handles navigation
 */
public class StartApp extends Application {
    private static StartApp instance = new StartApp();
    private static UI<Node> ui;
    private Stage stage;

    public static StartApp getInstance() {
        return instance;
    }

    public static void main(UI<Node> ui) {
        StartApp.ui = ui;
        launch();
    }

    public static void initForTest(UI<Node> ui) {
        StartApp.ui = ui;
    }

    @Override public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler(JavaFXUI::uncaughtException);

        try {
            if (getPreferences().getTheme() == JobsUITheme.Material) {
                replaceSceneContent(primaryStage, StartApp.class.getResource("StartMaterial.fxml"));
            } else {
                replaceSceneContent(primaryStage, StartApp.class.getResource("Start.fxml"));
            }
            primaryStage.setTitle("JobsUI");
            primaryStage.show();
            // after primaryStage.show() due to a reported bug (https://bugs.openjdk.java.net/browse/JDK-8132900).
            addStylesheet();
        } catch (Exception e) {
            Logger.getLogger(StartApp.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }

    public JobsUIPreferences getPreferences() {
        return ui.getPreferences();
    }

//    private static void setStylesheet() {
//        URL stylesheet = StartApp.class.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css");
//
//        try(InputStream inputStream = stylesheet.openStream()) {
//            File tmpCss = File.createTempFile("jobsui", ".css");
//            try {
//                String modenaCSS = ""; //IOUtils.toString(inputStream, Charset.forName("UTF-8"));
//                modenaCSS += "\n.root {\n" +
//                        "    -fx-base: rgb(50, 50, 50);\n" +
//                        "    -fx-background: rgb(50, 50, 50);\n" +
//                        "    -fx-control-inner-background:  rgb(50, 50, 50);\n" +
//                        "}";
//                FileUtils.write(tmpCss, modenaCSS, Charset.forName("UTF-8"));
//
////                PlatformImpl.setDefaultPlatformUserAgentStylesheet();
////                Application.setUserAgentStylesheet(null);
//                StyleManager.getInstance().addUserAgentStylesheet(tmpCss.toURI().toURL().toExternalForm());
//
////                Application.setUserAgentStylesheet(tmpCss.toURI().toURL().toExternalForm());
////                StyleManager.getInstance().addUserAgentStylesheet(tmpCss.toURI().toURL().toExternalForm());
//
////                setUserAgentStylesheet(tmpCss.toURI().toURL().toExternalForm());
//            } finally {
////                tmpCss.delete();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    void gotoRun(Project project, Job<Serializable> job) {
        JobUIRunner<Node> runner = new JobUIRunner<>(ui);
        try {
            runner.run(project, job);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void gotoEdit(ProjectFSXML projectXML) {
        EditProject editProject = new EditProject();
        stage = new Stage();
        try {
            editProject.start(ui, stage);
            editProject.edit(projectXML, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public void gotoStart() {
//        if (stage != null) {
//            stage.close();
//        }
//        primaryStage.show();
////        try {
////            replaceSceneContent("Start.fxml");
////        } catch (Exception ex) {
////            Logger.getLogger(StartApp.class.getName()).log(Level.SEVERE, null, ex);
////        }
////        stage.setWidth(200);
////        stage.setHeight(100);
//    }

    private static void addStylesheet() {
        switch (ui.getPreferences().getTheme()) {
            case Dark:
//                addStyleSheet("dark.css");
                break;
            default:
//                addStyleSheet("/resources/css/jfoenix-design.css");
//                addStyleSheet("/resources/css/jfoenix-fonts.css");
//                addStyleSheet("standard.css");
                break;
        }
    }

    private static void addStyleSheet(String resource) {
        URL url = StartApp.class.getResource(resource);
        StyleManager.getInstance().addUserAgentStylesheet(url.toExternalForm());
    }

    private static String resourceToURL(String resource) {
        URL url = StartApp.class.getResource(resource);
        return url.toExternalForm();
    }

    private void replaceSceneContent(Stage stage, URL fxml) throws Exception {
        Parent page = FXMLLoader.load(fxml, null, new JavaFXBuilderFactory());
        replaceSceneContent(stage, page);
    }

    private void replaceSceneContent(Stage stage, Parent page) throws Exception {
        Scene scene = stage.getScene();
        if (scene == null) {
            switch (ui.getPreferences().getTheme()) {
                case Dark:
                    scene = new Scene(page, 700, 450);
                    scene.getStylesheets().add(resourceToURL("dark.css"));
                    break;
                case Material:
                    JFXDecorator decorator = new JFXDecorator(stage, page, false, true, true);
                    addTitleToDecorator(stage, decorator);

                    scene = new Scene(decorator, 700, 450);
                    scene.getStylesheets().addAll(
                            resourceToURL("/resources/css/jfoenix-fonts.css"),
                            resourceToURL("/resources/css/jfoenix-design.css"),
                            resourceToURL("material.css")
                    );
                    break;
                default:
                    scene = new Scene(page, 700, 450);
                  scene.getStylesheets().add(resourceToURL("standard.css"));
                  break;
            }
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(page);
        }
//        page.setStyle("-fx-background-color:WHITE");
        stage.sizeToScene();
    }

    private static void addTitleToDecorator(Stage stage, JFXDecorator decorator) {
        Label titleLabel = new Label();
        titleLabel.getStyleClass().add(JobsUIFXStyles.TITLE_TEXT);

        // Aligning title to the left
        titleLabel.layoutXProperty().addListener((observable, oldValue, newValue) ->
            titleLabel.setTranslateX(-titleLabel.getLayoutX() + 10)
        );

        // when the title of the stage is changed then the label changes accordingly
        titleLabel.textProperty().bind(stage.titleProperty());

        ((HBox) decorator.getChildren().get(0)).getChildren().add(0, titleLabel);
    }

    Stage replaceSceneContent(Parent page, String title) throws Exception {
        Stage stage = new Stage();
        replaceSceneContent(stage, page);
        stage.setTitle(title);
        stage.getProperties().put("title", title);
//        Scene scene = stage.getScene();
//        if (scene == null) {
//            scene = new Scene(page, 700, 450);
////            scene.getStylesheets().add(LoginApp.class.getResource("demo.css").toExternalForm());
//            stage.setScene(scene);
//        } else {
//            stage.getScene().setRoot(page);
//        }
//        stage.sizeToScene();
//        this.stage = stage;
        return stage;
    }

    Stage getStage() {
        return stage;
    }
}
