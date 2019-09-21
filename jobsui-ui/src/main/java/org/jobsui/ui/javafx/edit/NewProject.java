package org.jobsui.ui.javafx.edit;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.jobsui.core.ui.UIButton;
import org.jobsui.core.xml.JobXMLImpl;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.core.xml.ProjectFSXMLImpl;
import org.jobsui.core.xml.ProjectXMLExporter;
import org.jobsui.ui.javafx.JavaFXUI;
import org.jobsui.ui.javafx.JavaFXUIFileChooser;
import org.jobsui.ui.javafx.LabeledField;
import org.jobsui.ui.javafx.StartApp;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NewProject {

    public static Optional<ProjectFSXML> show(JavaFXUI ui) {
        VBox root = new VBox(5);

        HBox buttons = new HBox(5);
        VBox.setVgrow(buttons, Priority.NEVER);
        buttons.setPadding(new Insets(5, 5, 5, 5));

        UIButton<Node> createButton = ui.createButton();
        createButton.setTitle("Create");
        buttons.getChildren().add(createButton.getComponent());

        root.getChildren().add(buttons);

        LabeledField labeledNamespace = new LabeledField(ui,"Namespace");
        labeledNamespace.setPadding(new Insets(10, 10, 0, 10));
        labeledNamespace.getField().setValue("myself.com");
        root.getChildren().add(labeledNamespace);

        LabeledField labeledName = new LabeledField(ui, "Project name");
        labeledName.setPadding(new Insets(10, 10, 0, 10));
        root.getChildren().add(labeledName);

        LabeledField labeledId = new LabeledField(ui,"Id");
        labeledId.setPadding(new Insets(10, 10, 0, 10));
        root.getChildren().add(labeledId);

        LabeledField labeledFolderName = new LabeledField(ui, "Folder name");
        labeledFolderName.setPadding(new Insets(10, 10, 0, 10));
        root.getChildren().add(labeledFolderName);

        JavaFXUIFileChooser labeledParentFolder = new JavaFXUIFileChooser(ui);
        labeledParentFolder.setFolder();
        labeledParentFolder.setTitle("Projects home");
        labeledParentFolder.getField().setValue(ui.getPreferences().getProjectsHome());
        labeledParentFolder.setPadding(new Insets(20, 10, 0, 10));
        root.getChildren().add(labeledParentFolder);

        labeledName.getField().getObservable().subscribe(newValue -> {
            if (newValue != null) {
//                Serializable labeledIdValue = labeledId.getField().getValue();
//                if (labeledIdValue == null || labeledIdValue.toString().isEmpty()) {
                    labeledId.getField().setValue(newValue.toString().toLowerCase().replace(" ", ""));
//                }

//                Serializable laveledFolderNameValue = labeledFolderName.getField().getValue();
//                if (laveledFolderNameValue == null || laveledFolderNameValue.toString().isEmpty()) {
                    labeledFolderName.getField().setValue(newValue);
//                }
            }
        });

        Stage stage = StartApp.getInstance().replaceSceneContent(root, "New project");

        AtomicReference<ProjectFSXML> project = new AtomicReference<>();

        createButton.getObservable().subscribe(event -> {
            File projectFolder = null;
            try {

                String parentFolder = Objects.toString(labeledParentFolder.getField().getValue());
                if (parentFolder == null || parentFolder.isEmpty()) {
                    ui.showMessage("Folder is mandatory.");
                    return;
                }

                String folder = Objects.toString(labeledFolderName.getField().getValue());
                if (folder == null || folder.isEmpty()) {
                    ui.showMessage("Folder name is mandatory.");
                    return;
                }

                String namespace = Objects.toString(labeledNamespace.getField().getValue());
                if (namespace == null || namespace.isEmpty()) {
                    ui.showMessage("Namespace is mandatory.");
                    return;
                }

                String id = Objects.toString(labeledId.getField().getValue());
                if (id == null || id.isEmpty()) {
                    ui.showMessage("Id is mandatory.");
                    return;
                }

                String name = Objects.toString(labeledName.getField().getValue());
                if (name == null || name.isEmpty()) {
                    ui.showMessage("Project name is mandatory.");
                    return;
                }

                projectFolder = new File(parentFolder + "/" + folder);

                if (projectFolder.exists()) {
                    ui.showMessage("Folder " + projectFolder + " already exists.");
                } else if (projectFolder.mkdirs()) {
                    ProjectFSXMLImpl projectFSXML = createProjectXML(projectFolder, namespace, id, name);

                    new ProjectXMLExporter().export(projectFSXML, projectFolder);
                    project.set(projectFSXML);
                    ui.getPreferences().registerOpenedProject(projectFolder.toURI().toURL(), name);
                    ui.getPreferences().setProjectsHome(new File(parentFolder));
                    stage.close();
                } else {
                    throw new RuntimeException("Cannot create folder project.");
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

    public static ProjectFSXMLImpl createProjectXML(File projectFolder, String namespace, String id, String name) throws Exception {
        ProjectFSXMLImpl projectFSXML = new ProjectFSXMLImpl(projectFolder,
                namespace + ":" + id, name, "1.0.0");

        JobXMLImpl jobXML = JobXMLImpl.createExampleJobXML("job", "Job");

        projectFSXML.addJob(jobXML);
        return projectFSXML;
    }

}
