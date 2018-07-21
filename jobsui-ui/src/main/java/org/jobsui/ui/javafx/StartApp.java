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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jobsui.core.CommandLineArguments;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.StartAction;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.ui.javafx.edit.EditProject;
import org.jobsui.ui.javafx.edit.NewProject;

import java.io.Serializable;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Application. This class handles navigation
 */
public class StartApp extends Application {
    private static final StartApp instance = new StartApp();
    private static JavaFXUI ui;
    private static CommandLineArguments arguments;
    private Stage stage;

    public static StartApp getInstance() {
        return instance;
    }

    public static void main(JavaFXUI ui, CommandLineArguments arguments) {
        StartApp.ui = ui;
        StartApp.arguments = arguments;
        launch();
    }

    public static void initForTest(JavaFXUI ui) {
        StartApp.ui = ui;
    }

    @Override public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler(JavaFXUI::uncaughtException);

        try {

            if (arguments.getAction() == StartAction.Run) {
                Project project = arguments.getProjectBuilder().build(arguments.getProjectXML(),
                        arguments.getBookmarksStore(), ui);
                Job<Serializable> job = project.getJob(arguments.getJob());
                if (job == null) {
                    throw new Exception(String.format("Cannot find job %s.", arguments.getJob()));
                }
                ui.getPreferences().registerOpenedProject(arguments.getProjectURL(),
                        project.getName());
                gotoRun(project, job);
            } else if (arguments.getAction() == StartAction.Edit) {
                if (arguments.getProjectFSXML() != null) {
                    ui.getPreferences().registerOpenedProject(arguments.getProjectURL(),
                            arguments.getProjectFSXML().getName());
                    gotoEdit(arguments.getProjectFSXML());
                } else {
                    gotoNew();
                }
            } else if (getPreferences().getTheme() == JobsUITheme.Material) {
                replaceSceneContent(primaryStage, StartApp.class.getResource("StartMaterial.fxml"));
                primaryStage.setTitle("JobsUI");
                primaryStage.show();
            } else {
                replaceSceneContent(primaryStage, StartApp.class.getResource("Start.fxml"));
                primaryStage.setTitle("JobsUI");
                primaryStage.show();
            }
        } catch (Exception e) {
            Logger.getLogger(StartApp.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }

    public JobsUIPreferences getPreferences() {
        return ui.getPreferences();
    }

    public static JavaFXUI getUi() {
        return ui;
    }

    void gotoRun(Project project, Job<Serializable> job) {
        JobUIRunner<Node> runner = new JobUIRunner<>(ui);
        try {
            runner.run(project, job);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void gotoNew() {
        Optional<ProjectFSXML> newProject = NewProject.show(ui);

        newProject.ifPresent(this::editProject);
    }

    void gotoEdit(ProjectFSXML projectXML) {
        stage = new Stage();
        try {
            try {
                editProject(projectXML);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void editProject(ProjectFSXML projectXML) {
        EditProject editProject = new EditProject();
        Parent root = editProject.getEditNode(ui);
        Stage stage = StartApp.getInstance().replaceSceneContent(root, projectXML.getName());
        editProject.edit(projectXML, false);
        editProject.loadPreferences(stage);
        stage.setOnHiding(event -> editProject.savePreferences(stage));
        stage.showAndWait();
    }

    private static String resourceToURL(String resource) {
        URL url = StartApp.class.getResource(resource);
        return url.toExternalForm();
    }

    private void replaceSceneContent(Stage stage, URL fxml) throws Exception {
        Parent page = FXMLLoader.load(fxml, null, new JavaFXBuilderFactory());
        replaceSceneContent(stage, page);
    }

    private void replaceSceneContent(Stage stage, Parent page) {
        Scene scene = stage.getScene();

        if (scene == null) {
            JobsUITheme theme = ui.getPreferences().getTheme();

            switch (theme) {
                case Material:
                    JFXDecorator decorator = new JFXDecorator(stage, page, false, true, true);
                    addTitleToDecorator(stage, decorator);

                    scene = new Scene(decorator, 700, 450);
                    break;
                case Dark:
                case Standard:
                    scene = new Scene(page, 700, 450);
                    break;
                default:
                    throw new IllegalStateException("Unknown theme '" + theme + "'.");
            }
            theme.applyToScene(scene);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(page);
        }
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

    public Stage replaceSceneContent(Parent page, String title) {
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
