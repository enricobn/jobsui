package org.jobsui.ui.javafx.edit;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.jobsui.core.ui.UIComponentRegistryImpl;
import org.jobsui.core.ui.UIComponentType;
import org.jobsui.core.xml.*;
import org.jobsui.ui.javafx.JavaFXUI;
import org.jobsui.ui.javafx.LabeledDirectoryChooser;
import org.jobsui.ui.javafx.LabeledField;
import org.jobsui.ui.javafx.StartApp;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NewProject {

    public static Optional<ProjectFSXML> show(JavaFXUI ui) {
        VBox root = new VBox(5);

        HBox buttons = new HBox(5);
        VBox.setVgrow(buttons, Priority.NEVER);
        buttons.setPadding(new Insets(5, 5, 5, 5));

        Button createButton = ui.createButton();
        createButton.setText("Create");
        buttons.getChildren().add(createButton);

        root.getChildren().add(buttons);

        LabeledDirectoryChooser labeledParentFolder = new LabeledDirectoryChooser(ui, "Parent folder");
        labeledParentFolder.getField().setText(".");
        labeledParentFolder.setPadding(new Insets(20, 10, 0, 10));
        root.getChildren().add(labeledParentFolder);

        LabeledField labeledFolderName = new LabeledField(ui, "Folder name");
        labeledFolderName.setPadding(new Insets(10, 10, 0, 10));
        root.getChildren().add(labeledFolderName);

        LabeledField labeledNamespace = new LabeledField(ui,"Namespace");
        labeledNamespace.setPadding(new Insets(10, 10, 0, 10));
        labeledNamespace.getField().setText("myself.com");
        root.getChildren().add(labeledNamespace);

        LabeledField labeledId = new LabeledField(ui,"Id");
        labeledId.setPadding(new Insets(10, 10, 0, 10));
        labeledId.getField().setText("myproject");
        root.getChildren().add(labeledId);

        LabeledField labeledName = new LabeledField(ui, "Project name");
        labeledName.setPadding(new Insets(10, 10, 0, 10));
        root.getChildren().add(labeledName);

        Stage stage = StartApp.getInstance().replaceSceneContent(root, "New project");

        AtomicReference<ProjectFSXML> project = new AtomicReference<>();

        createButton.setOnAction(event -> {
            File projectFolder = null;
            try {

                String parentFolder = labeledParentFolder.getField().getText();
                if (parentFolder == null || parentFolder.isEmpty()) {
                    ui.showMessage("Folder is mandatory.");
                    return;
                }

                String folder = labeledFolderName.getField().getText();
                if (folder == null || folder.isEmpty()) {
                    ui.showMessage("Folder name is mandatory.");
                    return;
                }

                String namespace = labeledNamespace.getField().getText();
                if (namespace == null || namespace.isEmpty()) {
                    ui.showMessage("Namespace is mandatory.");
                    return;
                }

                String id = labeledId.getField().getText();
                if (id == null || id.isEmpty()) {
                    ui.showMessage("Id is mandatory.");
                    return;
                }

                String name = labeledName.getField().getText();
                if (name == null || name.isEmpty()) {
                    ui.showMessage("Project name is mandatory.");
                    return;
                }

                projectFolder = new File(parentFolder + "/" + folder);

                if (projectFolder.exists()) {
                    ui.showMessage("Folder " + projectFolder + " already exists.");
                } else if (projectFolder.mkdir()) {
                    ProjectFSXMLImpl projectFSXML = createProjectXML(projectFolder, namespace, id, name);

                    new ProjectXMLExporter().export(projectFSXML, projectFolder);
                    project.set(projectFSXML);
                    ui.getPreferences().registerOpenedProject(projectFolder.toURI().toURL(), name);
                    stage.close();
                }
            } catch (Exception e) {
                ui.showError("Cannot create project.", e);
                if (projectFolder != null && projectFolder.exists()) {
                    try {
                        FileUtils.deleteDirectory(projectFolder);
                    } catch (IOException e1) {
                        ui.showError("Cannot delete incomplete project's folder " + projectFolder + ". Delete it manually!", e);
                        e1.printStackTrace();
                    }
                }
            }

        });

        stage.showAndWait();

        if (project.get() != null) {
            return Optional.of(project.get());
        } else {
            return Optional.empty();
        }
    }

    private static ProjectFSXMLImpl createProjectXML(File projectFolder, String namespace, String id, String name) throws Exception {
        ProjectFSXMLImpl projectFSXML = new ProjectFSXMLImpl(projectFolder,
                namespace + ":" + id, name, "1.0.0");

        JobXMLImpl jobXML = new JobXMLImpl("newjob", "NewJob", "1.0.0");
        SimpleParameterXML parameter = new SimpleParameterXML("message", "Message",
                UIComponentRegistryImpl.Value);
        parameter.setOnInitScript("component.setValue('Hello world')");
        jobXML.add(parameter);
        jobXML.setRunScript("println(\"${message}\")");

        projectFSXML.addJob("newjob.xml", jobXML);
        return projectFSXML;
    }

}
