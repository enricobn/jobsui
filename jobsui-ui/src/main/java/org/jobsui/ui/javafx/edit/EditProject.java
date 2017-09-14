package org.jobsui.ui.javafx.edit;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.UIComponentType;
import org.jobsui.core.xml.*;
import org.jobsui.ui.javafx.JavaFXUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by enrico on 10/9/16.
 */
public class EditProject {
    private TreeView<EditItem> itemsTree;
    private ItemDetail itemDetail;
    private ProjectFSXML projectXML = null;
    private List<String> originalJobs = null;
    private List<String> originalScriptLocations = null;
    private Button saveButton;
    private JobsUIPreferences preferences;
    private JavaFXUI ui;
    private SplitPane splitPane;

    public Parent getEditNode(JavaFXUI ui) {
        this.ui = ui;
        preferences = ui.getPreferences();
        VBox root = new VBox(5);
        HBox buttons = new HBox(5);
        VBox.setVgrow(buttons, Priority.NEVER);
        buttons.setPadding(new Insets(5, 5, 5, 5));

        saveButton = ui.createButton();
        saveButton.setText("Save");
        saveButton.setOnAction(event -> {
            try {
                for (String originalJob : originalJobs) {
                    File file = new File(projectXML.getFolder(), originalJob);
                    if (file.exists()) {
                        file.delete();
                    }
                }

                for (String location : originalScriptLocations) {
                    File locationRoot = new File(projectXML.getFolder(), location);
                    FileUtils.deleteDirectory(locationRoot);
                }

                ProjectXMLExporter exporter = new ProjectXMLExporter();
                exporter.export(projectXML, projectXML.getFolder());
            } catch (Exception e) {
                ui.showError("Error saving project.", e);
            }
        });
        buttons.getChildren().add(saveButton);

        Button saveAsButton = ui.createButton();
        saveAsButton.setText("Save as");
        saveAsButton.setOnAction(event -> {
            try {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Save project");
                chooser.setInitialDirectory(projectXML.getFolder());

                File file = chooser.showDialog(null);

                if (file != null) {
                    if (file.exists()) {
                        String[] files = file.list();
                        if (files != null && files.length > 0) {
                            ui.showMessage("Directory is not empty.");
                            return;
                        }
                    } else {
                        file.mkdir();
                    }
                    ProjectXMLExporter exporter = new ProjectXMLExporter();
                    exporter.export(projectXML, file);
                    saveButton.setDisable(false);
                    projectXML.setFolder(file);
                    setProjectXML(projectXML);
                }
            } catch (Exception e) {
                ui.showError("Error saving project.", e);
            }
        });
        buttons.getChildren().add(saveAsButton);

        root.getChildren().add(buttons);

        itemsTree = new TreeView<>();
//        itemsTree.setCellFactory(param -> new TextFieldTreeCellImpl());
        itemsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    itemDetail.setSelectedItem(newValue);
//                    newValue.getValue().onSelect();
                } catch (Throwable e) {
                    ui.showError("Error", e);
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();

        itemsTree.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                TreeItem<EditItem> selected = itemsTree.getSelectionModel().getSelectedItem();

                //item is selected - this prevents fail when clicking on empty space
                if (selected != null) {
                    populateContextMenu(contextMenu, selected);
                    if (contextMenu.getItems().isEmpty()) {
                        contextMenu.hide();
                    } else {
                        //show menu
                        contextMenu.show(itemsTree, e.getScreenX(), e.getScreenY());
                    }
                }
            } else {
                //any other click cause hiding menu
                contextMenu.hide();
            }
        });

        itemDetail = new ItemDetail(ui);
        itemDetail.setPadding(new Insets(5, 5, 5, 5));

        splitPane = new SplitPane(itemsTree, itemDetail);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        root.getChildren().add(splitPane);

        Label status = new Label();
        root.getChildren().add(status);

        return root;
    }

    private TreeItem<EditItem> loadProject(ProjectFSXML projectXML) {
        TreeItem<EditItem> root = new TreeItem<>(new EditItem(ItemType.Project, projectXML::getName, projectXML));

        TreeItem<EditItem> libraries = new TreeItem<>(new EditItem(ItemType.Libraries, () -> "libraries", projectXML));
        root.getChildren().add(libraries);
        projectXML.getLibraries().stream()
                .map(l -> new EditItem(ItemType.Library, () -> l, l))
                .map(TreeItem::new)
                .forEach(treeItem -> libraries.getChildren().add(treeItem));

        for (String location : projectXML.getScriptsLocations()) {
            TreeItem<EditItem> locationItem = new TreeItem<>(new EditItem(ItemType.Scripts, () -> location, location));
            root.getChildren().add(locationItem);
            projectXML.getScriptFilesNames(location).stream()
                    .map(fileName -> new EditItem(ItemType.ScriptFile, () -> fileName, fileName))
                    .map(TreeItem::new)
                    .forEach(treeItem -> locationItem.getChildren().add(treeItem));
        }

        projectXML.getJobs().stream()
                .map(projectXML::getJobXML)
                .sorted(Comparator.comparing(JobXML::getName))
                .map(this::createJobTreeItem)
                .forEach(root.getChildren()::add);
        return root;
    }

    private TreeItem<EditItem> createJobTreeItem(JobXML job) {
        TreeItem<EditItem> result = new TreeItem<>(new EditItem(ItemType.Job, job::getName, job));

        addParameters(result, job, "parameters", ItemType.Parameter, job.getSimpleParameterXMLs());
        addParameters(result, job, "expressions", ItemType.Expression, job.getExpressionXMLs());
        addParameters(result, job, "calls", ItemType.Call, job.getCallXMLs());

        result.setExpanded(true);
        return result;
    }

    private void addParameters(TreeItem<EditItem> result, JobXML jobXML, String containerText, ItemType itemType,
                               List<? extends ParameterXML> parametersList) {
        TreeItem<EditItem> parameters = new TreeItem<>(new EditItem(ItemType.Parameters, () -> containerText, jobXML));
        parameters.setExpanded(true);
        result.getChildren().add(parameters);

        parametersList.forEach(parameter -> addParameter(parameters, itemType, parameter, jobXML));
    }

    private void addParameter(TreeItem<EditItem> parameters, ItemType itemType, ParameterXML parameterDef, JobXML jobXML) {
        TreeItem<EditItem> parameterTI = new TreeItem<>(new EditItem(itemType, parameterDef::getName, parameterDef));
        parameters.getChildren().add(parameterTI);
        TreeItem<EditItem> dependencies = new TreeItem<>(new EditItem(ItemType.Dependencies, () -> "dependencies", parameterDef));
        parameterTI.getChildren().add(dependencies);
        parameterDef.getDependencies().stream()
                .map(depKey -> {
                    ParameterXML dep = jobXML.getParameter(depKey);
                    if (dep == null) {
                        throw new RuntimeException("Cannot find parameter for dependency '" + depKey + "'.");
                    }
                    return dep;
                })
                .map(dep -> new EditItem(ItemType.Dependency, dep::getName, dep.getKey()))
                .map(TreeItem::new)
                .forEach(dependencies.getChildren()::add);
    }

    public void edit(ProjectFSXML projectXML, boolean isNew) {
        setProjectXML(projectXML);

        if (isNew) {
            saveButton.setDisable(true);
        }

        TreeItem<EditItem> root = loadProject(projectXML);

        Platform.runLater(() -> {
            itemsTree.setRoot(root);
            root.setExpanded(true);
        });
    }

    private void setProjectXML(ProjectFSXML projectXML) {
        this.projectXML = projectXML;
        this.originalJobs = new ArrayList<>(projectXML.getJobs());
        this.originalScriptLocations = new ArrayList<>(projectXML.getScriptsLocations());
//        stage.setTitle(projectXML.getName());
    }

    public void savePreferences(Stage stage) {
        preferences.setEditDividerPosition(splitPane.getDividerPositions()[0]);
        preferences.setEditWidth(stage.getWidth());
        preferences.setEditHeight(stage.getHeight());
    }

    public void loadPreferences(Stage stage) {
        splitPane.setDividerPosition(0, preferences.getEditDividerPosition());
        stage.setWidth(preferences.getEditWidth());
        stage.setHeight(preferences.getEditHeight());
    }

    enum ItemType {
        Project, ScriptFile, Job, Parameter, Expression, Dependency, Dependencies, Parameters, Scripts, Libraries,
        Library, Call
    }

    private void populateContextMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        contextMenu.getItems().clear();
        EditItem item = treeItem.getValue();

        if (item.itemType == ItemType.Parameters) {
            addParameterMenu(contextMenu, treeItem);

        } else if (item.itemType == ItemType.Dependencies) {
            ParameterXML parameterXML = (ParameterXML) item.payload;
            List<String> dependencies = parameterXML.getDependencies();
            JobXML jobXML = findAncestorPayload(treeItem, ItemType.Job);

            if (jobXML == null) {
                ui.showMessage("Cannot find job for " + item);
                return;
            }

            List<String> parameters = getAllParameters(jobXML);
            parameters.removeAll(dependencies);

            if (!parameters.isEmpty()) {
                Menu addDependency = new Menu("Add dependency");

                for (String dependency : parameters) {
                    if (parameterXML.getKey().equals(dependency)) {
                        continue;
                    }
                    ParameterXML parameter = jobXML.getParameter(dependency);
                    String name = parameter.getName();
                    MenuItem dependencyMenuItem = new MenuItem(name);
                    dependencyMenuItem.setOnAction(e -> {
                        parameterXML.addDependency(dependency);
                        TreeItem<EditItem> newDep = new TreeItem<>(new EditItem(ItemType.Dependency,
                                () -> name, dependency));
                        treeItem.getChildren().add(newDep);
                    });
                    addDependency.getItems().add(dependencyMenuItem);
                }

                contextMenu.getItems().add(addDependency);
            }
        } else if (item.itemType == ItemType.Dependency) {
            MenuItem delete = new MenuItem("Delete");
            contextMenu.getItems().add(delete);
            delete.setOnAction(t -> {
                ParameterXML parameterXML = (ParameterXML) treeItem.getParent().getValue().payload;
                treeItem.getParent().getChildren().remove(treeItem);
                parameterXML.removeDependency((String)item.payload);
            });
        }
    }

    private void addParameterMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, ItemType.Job);
        if (jobXML == null) {
            return;
        }

        Menu addParameter = new Menu("Add parameter");
        contextMenu.getItems().add(addParameter);

        for (UIComponentType uiComponentType : UIComponentType.values()) {
            MenuItem addParameterType = new MenuItem(uiComponentType.name());
            addParameter.getItems().add(addParameterType);
            addParameterType.setOnAction(e -> {
                SimpleParameterXML parameter = new SimpleParameterXML("newKey", "newName", uiComponentType);
                try {
                    jobXML.add(parameter);
                    addParameter(treeItem, ItemType.Parameter, parameter, jobXML);
                } catch (Exception e1) {
                    ui.showError("Error adding new parameter.", e1);
                }
            });
        }

    }

    static <T> T findAncestorPayload(TreeItem<EditItem> treeItem, ItemType... itemTypes) {
        TreeItem<EditItem> ancestor = treeItem.getParent();
        while (ancestor != null) {
            EditItem value = ancestor.getValue();
            if (value != null) {
                for (ItemType itemType : itemTypes) {
                    if (value.itemType == itemType) {
                        return (T) value.payload;
                    }
                }
            }
            ancestor = ancestor.getParent();
        }
        return null;
    }

    private static List<String> getAllParameters(JobXML jobXML) {
        return Stream.concat(Stream.concat(jobXML.getCallXMLs().stream(),
                jobXML.getExpressionXMLs().stream()),
                jobXML.getSimpleParameterXMLs().stream())
                .map(ParameterXML::getKey).collect(Collectors.toList());
    }

    private static TreeItem<EditItem> findItem(TreeItem<EditItem> root, EditItem item) {
        if (root.getValue() == item) {
            return root;
        }
        for (TreeItem<EditItem> child : root.getChildren()) {
            TreeItem<EditItem> found = findItem(child, item);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}