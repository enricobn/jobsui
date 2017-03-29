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

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.job.Job;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Application. This class handles navigation
 */
public class StartApp extends Application {
    private static StartApp instance;
    private Stage stage;

    public StartApp() {
        instance = this;
    }

    public static StartApp getInstance() {
        return instance;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            gotoStart();
            primaryStage.show();
        } catch (Exception ex) {
            Logger.getLogger(StartApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void gotoRun(Job<Serializable> job) {
        VBox vBox = new VBox();
        Button button = new Button("To login");
        button.onActionProperty().setValue(event -> gotoStart());

        vBox.getChildren().add(button);
        try {
            replaceSceneContent(vBox);
        } catch (Exception ex) {
            Logger.getLogger(StartApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        stage.setWidth(300);
        stage.setHeight(500);
    }

    private void gotoStart() {
        try {
            replaceSceneContent("Start.fxml");
        } catch (Exception ex) {
            Logger.getLogger(StartApp.class.getName()).log(Level.SEVERE, null, ex);
        }
//        stage.setWidth(200);
//        stage.setHeight(100);
    }

    private Parent replaceSceneContent(String fxml) throws Exception {
        Parent page = FXMLLoader.load(StartApp.class.getResource(fxml), null, new JavaFXBuilderFactory());
        return replaceSceneContent(page);
    }

    private Parent replaceSceneContent(Parent page) throws Exception {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(page, 700, 450);
//            scene.getStylesheets().add(LoginApp.class.getResource("demo.css").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(page);
        }
        stage.sizeToScene();
        return page;
    }
}
