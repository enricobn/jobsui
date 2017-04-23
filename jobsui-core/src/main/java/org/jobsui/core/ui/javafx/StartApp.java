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
package org.jobsui.core.ui.javafx;

import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jobsui.core.JobsUIMainParameters;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.job.Project;
import org.jobsui.core.edit.EditProject;
import org.jobsui.core.job.Job;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.xml.ProjectFSXML;

import java.io.Serializable;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main Application. This class handles navigation
 */
public class StartApp extends Application {
    private static StartApp instance = new StartApp();
    private static JobsUIPreferences preferences;
    private JobsUIMainParameters parameters;
    private Stage stage;

    public static StartApp getInstance() {
        return instance;
    }

    public static void main(JobsUIPreferences preferences, String[] args) {
        StartApp.preferences = preferences;
        launch(args);
    }

    public static void initForTest(JobsUIPreferences preferences) {
        StartApp.preferences = preferences;
    }

    @Override public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler(JavaFXUI::uncaughtException);

        String[] args = getParameters().getUnnamed().toArray(new String[0]);

        JobsUIMainParameters.parse(args,
                p -> this.parameters = p,
                errors -> {
                    Stage stage = JavaFXUI.getErrorStage("Error starting application",
                        errors.stream().collect(Collectors.joining("\n")));
                    if (stage != null) {
                        stage.showAndWait();
                        System.exit(1);
                    }
                });

        try {
            replaceSceneContent(primaryStage, StartApp.class.getResource("Start.fxml"));
            primaryStage.setTitle("JobsUI");
            primaryStage.show();
            // after primaryStage.show() due to a reported bug (https://bugs.openjdk.java.net/browse/JDK-8132900).
            addStylesheet();
        } catch (Exception e) {
            Logger.getLogger(StartApp.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }

    public JobsUIMainParameters getJobsUIParameters() {
        return parameters;
    }

    public JobsUIPreferences getPreferences() {
        return preferences;
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
        JobUIRunner<Node> runner = new JobUIRunner<>(new JavaFXUI());
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
            editProject.start(stage);
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
        String resource;
        switch (preferences.getTheme()) {
            case Dark:
                resource = "dark.css";
                break;
            default:
                resource = "standard.css";
                break;
        }
        String url = StartApp.class.getResource(resource).toExternalForm();
        StyleManager.getInstance().addUserAgentStylesheet(url);
    }

    private void replaceSceneContent(Stage stage, URL fxml) throws Exception {
        Parent page = FXMLLoader.load(fxml, null, new JavaFXBuilderFactory());
        replaceSceneContent(stage, page);
    }

    private void replaceSceneContent(Stage stage, Parent page) throws Exception {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(page, 700, 450);
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
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
