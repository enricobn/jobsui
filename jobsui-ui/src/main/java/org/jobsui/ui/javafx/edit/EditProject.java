package org.jobsui.ui.javafx.edit;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.runner.JobsUIValidationResult;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIButton;
import org.jobsui.core.ui.UIComponentRegistry;
import org.jobsui.core.ui.UIComponentType;
import org.jobsui.core.utils.JobsUIUtils;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/9/16.
 */
public class EditProject {
    private TreeView<EditItem> itemsTree;
    private ItemDetail itemDetail;
    private ProjectFSXML projectXML = null;
    private List<String> originalJobFiles = null;
    private List<String> originalScriptLocations = null;
    private UIButton<Node> saveButton;
    private JobsUIPreferences preferences;
    private UI ui;
    private SplitPane splitPane;
    private UIComponentRegistry uiComponentRegistry;
    private boolean valid = true;

    public EditProject() {
    }

    static void validate(TreeItem<EditItem> treeItem, boolean anchestors) {
        Object payload = treeItem.getValue().payload;

        if (payload instanceof ValidatingXML) {
            ValidatingXML validatingXML = (ValidatingXML) payload;
            List<String> validate = validatingXML.validate();
            if (!validate.isEmpty()) {
                Label label = new Label("?");
                label.setTextFill(Color.RED);
                label.setTooltip(new Tooltip(String.join("\n", validate)));
                treeItem.setGraphic(label);
                treeItem.getValue().setValid(false);
            } else {
                treeItem.setGraphic(null);
                treeItem.getValue().setValid(true);
            }
        }

        if (anchestors && treeItem.getParent() != null) {
            validate(treeItem.getParent(), true);
        }
    }

    public Parent getEditNode(UI ui) {
        this.ui = ui;
        preferences = ui.getPreferences();
        VBox root = new VBox(5);
        HBox buttons = new HBox(5);
        VBox.setVgrow(buttons, Priority.NEVER);
        buttons.setPadding(new Insets(5, 5, 5, 5));

        saveButton = ui.createButton();
        saveButton.setTitle("Save");
        saveButton.getObservable().subscribe(event -> {

            if (!this.valid) {
                ui.showMessage("There are some invalid values.");
                return;
            }

            // I backup the current project
            File tempDir;
            try {
                tempDir = JobsUIUtils.createTempDir("jobsui", projectXML.getFolder().getName());
                FileUtils.copyDirectory(projectXML.getFolder(), tempDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                for (String originalJobFile : originalJobFiles) {
                    File file = new File(projectXML.getFolder(), originalJobFile);
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

                FileUtils.deleteDirectory(tempDir);

                setNotChanged();
            } catch (Exception e) {
                // I restore the project
                try {
                    FileUtils.deleteDirectory(projectXML.getFolder());
                    FileUtils.copyDirectory(tempDir, projectXML.getFolder());
                } catch (IOException e1) {
                    throw new RuntimeException(e);
                }
                ui.showError("Error saving project.", e);
            }
        });
        buttons.getChildren().add(saveButton.getComponent());

        UIButton<Node> saveAsButton = ui.createButton();
        saveAsButton.setTitle("Save as");
        saveAsButton.getObservable().subscribe(event -> {
            try {
                if (!this.valid) {
                    ui.showMessage("There are some invalid values.");
                    return;
                }

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
                    saveButton.setEnabled(true);
                    projectXML.setFolder(file);
                    setProjectXML(projectXML);

                    setNotChanged();
                }
            } catch (Exception e) {
                ui.showError("Error saving project.", e);
            }
        });
        buttons.getChildren().add(saveAsButton.getComponent());

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

    public boolean isChanged() {
        AtomicBoolean changed = new AtomicBoolean();

        traverse(itemsTree.getRoot(), it -> {
            if (it.getValue().isChanged()) {
                changed.set(true);
            }
        });

        return changed.get();
    }

    private void traverse(Consumer<TreeItem<EditItem>> consumer) {
        traverse(itemsTree.getRoot(), consumer);
    }

    private void traverse(TreeItem<EditItem> treeItem, Consumer<TreeItem<EditItem>> consumer) {
        consumer.accept(treeItem);

        for (TreeItem<EditItem> child : treeItem.getChildren()) {
            traverse(child, consumer);
        }
    }

    private void setNotChanged() {
        setNotChanged(itemsTree.getRoot());
        itemsTree.refresh();
    }

    private void setNotChanged(TreeItem<EditItem> treeItem) {
        treeItem.getValue().setChanged(false);

        for (TreeItem<EditItem> child : treeItem.getChildren()) {
            setNotChanged(child);
        }
    }

    private TreeItem<EditItem> loadProject(ProjectFSXML projectXML) {
        this.uiComponentRegistry = projectXML.getUiComponentRegistry();
        itemDetail.setUiComponentRegistry(uiComponentRegistry);

        TreeItem<EditItem> root = getTreeItem(ItemType.Project, projectXML);

        TreeItem<EditItem> libraries = getTreeItem(ItemType.Libraries, projectXML);
        root.getChildren().add(libraries);
        projectXML.getLibraries().stream()
                .map(l -> getTreeItem(ItemType.Library, l))
                .forEach(treeItem -> libraries.getChildren().add(treeItem));

        TreeItem<EditItem> scriptsItem = getTreeItem(ItemType.Scripts, projectXML);
        root.getChildren().add(scriptsItem);
        for (String location : projectXML.getScriptsLocations()) {
            TreeItem<EditItem> locationItem = getTreeItem(ItemType.ScriptsLocation, location);
            scriptsItem.getChildren().add(locationItem);
            projectXML.getScriptFilesNames(location).stream()
                    .map(fileName -> getTreeItem(ItemType.ScriptFile, fileName))
                    .forEach(treeItem -> locationItem.getChildren().add(treeItem));
            handleTreeItemChange(locationItem);
        }

        projectXML.getJobs().stream()
                .sorted(Comparator.comparing(JobXML::getName))
                .map(this::createJobTreeItem)
                .forEach(root.getChildren()::add);

        libraries.getChildren().addListener((ListChangeListener<TreeItem<EditItem>>) change -> libraries.getValue().setChanged(true));

        handleTreeItemChange(root);

        return root;
    }

    private TreeItem<EditItem> getTreeItem(ItemType project, Object payload) {
        EditItem editItem = new EditItem(project, payload);
        editItem.getObservable().subscribe(editItem1 -> {
            AtomicBoolean valid = new AtomicBoolean(true);

            traverse(it -> {
                if (!it.getValue().isValid()) {
                    valid.set(false);
                }
            });

            EditProject.this.valid =  valid.get();
        });

        TreeItem<EditItem> treeItem = new TreeItem<>(editItem);

        return treeItem;
    }

    private TreeItem<EditItem> createJobTreeItem(JobXML job) {
        TreeItem<EditItem> result = getTreeItem(ItemType.Job, job);

        addParameters(result, job, ItemType.Parameters, ItemType.Parameter, job.getSimpleParameterXMLs());
        addParameters(result, job, ItemType.Expressions, ItemType.Expression, job.getExpressionXMLs());
        addParameters(result, job, ItemType.Calls, ItemType.Call, job.getCallXMLs());

        addWizard(job, result);

        result.setExpanded(true);

        return result;
    }

    private void addWizard(JobXML job, TreeItem<EditItem> result) {
        TreeItem<EditItem> wizardSteps = getTreeItem(ItemType.WizardSteps, job.getWizardSteps());

        result.getChildren().add(wizardSteps);

        job.getWizardSteps().forEach(w -> {
            TreeItem<EditItem> wizardStep = getTreeItem(ItemType.WizardStep, w);
            wizardSteps.getChildren().add(wizardStep);
            TreeItem<EditItem> dependencies = getTreeItem(ItemType.WizardStepDependencies, w);
            wizardStep.getChildren().add(dependencies);

            w.getDependencies().forEach(d -> {
                TreeItem<EditItem> dependency = getTreeItem(ItemType.WizardStepDependency, job.getParameter(d));
                dependencies.getChildren().add(dependency);
            });

            handleTreeItemChange(dependencies);

        });
        handleTreeItemChange(wizardSteps);

    }

    private void addParameters(TreeItem<EditItem> result, JobXML jobXML, ItemType containerType, ItemType itemType,
                               List<? extends ParameterXML> parametersList) {
        TreeItem<EditItem> parameters = getTreeItem(containerType, parametersList);
        parameters.setExpanded(true);
        result.getChildren().add(parameters);

        parametersList.forEach(parameter -> addParameter(parameters, itemType, parameter, jobXML));

        handleTreeItemChange(parameters);
    }

    private static void handleTreeItemChange(TreeItem<EditItem> treeItem) {
        treeItem.getChildren().addListener((ListChangeListener<TreeItem<EditItem>>) change -> treeItem.getValue().setChanged(true));
    }

    private void addParameter(TreeItem<EditItem> parameters, ItemType itemType, ParameterXML parameterDef, JobXML jobXML) {
        TreeItem<EditItem> parameterTI = getTreeItem(itemType, parameterDef);
        parameters.getChildren().add(parameterTI);
        TreeItem<EditItem> dependencies = getTreeItem(ItemType.Dependencies, parameterDef);
        parameterTI.getChildren().add(dependencies);
        parameterDef.getDependencies().stream()
                .map(depKey -> {
                    ParameterXML dep = jobXML.getParameter(depKey);
                    if (dep == null) {
                        throw new RuntimeException("Cannot find parameter for dependency '" + depKey + "'.");
                    }
                    return dep;
                })
                .map(dep -> getTreeItem(ItemType.Dependency, dep))
                .forEach(dependencies.getChildren()::add);
        handleTreeItemChange(dependencies);
    }

    public void edit(ProjectFSXML projectXML, boolean isNew) {
        setProjectXML(projectXML);

        if (isNew) {
            saveButton.setEnabled(false);
        }

        TreeItem<EditItem> root = loadProject(projectXML);

        Platform.runLater(() -> {
            itemsTree.setRoot(root);
            root.setExpanded(true);

            validate();
        });
    }

    private void setProjectXML(ProjectFSXML projectXML) {
        this.projectXML = projectXML;
        this.originalJobFiles = projectXML.getJobs().stream()
                .map(job -> JobXMLImpl.getFileName(job.getId()))
                .collect(Collectors.toList());
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

    private void populateContextMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        contextMenu.getItems().clear();
        EditItem item = treeItem.getValue();

        if (item.itemType == ItemType.Parameters) {
            populateParametersMenu(ui, uiComponentRegistry, contextMenu, treeItem);
        } else if (item.itemType == ItemType.Parameter) {
            populateParameterMenu(ui, contextMenu, treeItem);
        } else if (item.itemType == ItemType.Dependencies) {
            populateDependenciesMenu(ui, contextMenu, treeItem);
        } else if (item.itemType == ItemType.Dependency) {
            populateDependencyMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Expressions) {
            populateExpressionsMenu(ui, contextMenu, treeItem);
        } else if (item.itemType == ItemType.Expression) {
            populateParameterMenu(ui, contextMenu, treeItem);
        } else if (item.itemType == ItemType.Calls) {
            populateCallsMenu(ui, contextMenu, treeItem);
        } else if (item.itemType == ItemType.Call) {
            populateParameterMenu(ui, contextMenu, treeItem);
        } else if (item.itemType == ItemType.ScriptsLocation) {
            populateScriptsLocationMenu(ui, contextMenu, treeItem);
        } else if (item.itemType == ItemType.ScriptFile) {
            populateScriptFileMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Libraries) {
            populateLibrariesMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Library) {
            populateLibraryMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.WizardSteps) {
            populateWizardStepsMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.WizardStep) {
            populateWizardStepMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.WizardStepDependencies) {
            populateWizardStepDependenciesMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.WizardStepDependency) {
            populateWizardStepDependencyMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Project) {
            populateProjectMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Job) {
            populateJobMenu(contextMenu, treeItem);
        }
    }

    private void populateJobMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXML projectXML = findAncestorPayload(treeItem, ProjectFSXML.class);

        JobXML jobXML = (JobXML) treeItem.getValue().payload;

        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            projectXML.getJobs().remove(jobXML);
            treeItem.getParent().getChildren().remove(treeItem);
        });

        MenuItem copy = new MenuItem("Copy");
        contextMenu.getItems().add(copy);
        copy.setOnAction(t -> {
            JobXML copied = jobXML.copy();
            projectXML.addJob(copied);

            TreeItem<EditItem> item = createJobTreeItem(copied);
            treeItem.getParent().getChildren().add(item);
        });
    }

    private void populateProjectMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXMLImpl projectXML = (ProjectFSXMLImpl) treeItem.getValue().payload;

        MenuItem add = new MenuItem("Add job");
        contextMenu.getItems().add(add);
        add.setOnAction(t -> {
            JobXML jobXML;
            try {
                String id = nextAvailableKey(projectXML.getJobs(), JobXML::getId, "newJob");
                jobXML = JobXMLImpl.createExampleJobXML(id, "New job");
                jobXML.setId(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            projectXML.addJob(jobXML);

            TreeItem<EditItem> item = createJobTreeItem(jobXML);

            treeItem.getChildren().add(item);
        });

    }

    private static void populateScriptFileMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXML projectXML = findAncestorPayload(treeItem, ProjectFSXML.class);

        String location = findAncestorPayload(treeItem, ItemType.ScriptsLocation);

        String scriptFile = (String) treeItem.getValue().payload;

        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            projectXML.removeScriptFile(location, scriptFile);
            treeItem.getParent().getChildren().remove(treeItem);
        });

    }

    private static void populateWizardStepDependencyMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        WizardStep wizardStep = findAncestorPayload(treeItem, WizardStep.class);

        String parameterKey = ((ParameterXML) treeItem.getValue().payload).getKey();

        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            wizardStep.getDependencies().remove(parameterKey);
            treeItem.getParent().getChildren().remove(treeItem);
        });
    }

    private void populateWizardStepDependenciesMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXML jobXML = findAncestorPayload(treeItem, JobXML.class);

        WizardStep wizardStep = (WizardStep) treeItem.getValue().payload;

        List<String> parameters = JobXML.getSimpleParametersKeys(jobXML);

        for (WizardStep step : jobXML.getWizardSteps()) {
            parameters.removeAll(step.getDependencies());
        }

        if (!parameters.isEmpty()) {
            Menu addDependency = new Menu("Add");

            for (String dependency : parameters) {
                ParameterXML parameter = jobXML.getParameter(dependency);
                String name = parameter.getName();
                MenuItem dependencyMenuItem = new MenuItem(name);
                dependencyMenuItem.setOnAction(e -> {
                    wizardStep.getDependencies().add(dependency);
                    TreeItem<EditItem> newDep = getTreeItem(ItemType.WizardStepDependency, parameter);
                    treeItem.getChildren().add(newDep);
                });
                addDependency.getItems().add(dependencyMenuItem);
            }

            contextMenu.getItems().add(addDependency);
        }

    }

    private static void populateWizardStepMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXML jobXML = findAncestorPayload(treeItem, JobXML.class);
        if (jobXML == null) {
            return;
        }

        WizardStep wizardStep = (WizardStep) treeItem.getValue().payload;

        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            jobXML.getWizardSteps().remove(wizardStep);
            treeItem.getParent().getChildren().remove(treeItem);
        });
    }

    private void populateWizardStepsMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXML jobXML = findAncestorPayload(treeItem, JobXML.class);
        if (jobXML == null) {
            return;
        }

        MenuItem add = new MenuItem("Add");
        contextMenu.getItems().add(add);
        add.setOnAction(t -> {
            WizardStepImpl wizardStep = new WizardStepImpl();
            wizardStep.setName("New wizard step");
            jobXML.getWizardSteps().add(wizardStep);
            TreeItem<EditItem> item = getTreeItem(ItemType.WizardStep, wizardStep);
            TreeItem<EditItem> dependencies = getTreeItem(ItemType.WizardStepDependencies, wizardStep);
            item.getChildren().add(dependencies);

            treeItem.getChildren().add(item);
        });
    }

    private static void populateLibraryMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectXML projectXML = findAncestorPayload(treeItem, ProjectXML.class);
        if (projectXML == null) {
            return;
        }

        ProjectLibraryXML library = (ProjectLibraryXML) treeItem.getValue().payload;

        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            projectXML.getLibraries().remove(library);
            treeItem.getParent().getChildren().remove(treeItem);
        });
    }

    private void populateLibrariesMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectXML projectXML = findAncestorPayload(treeItem, ProjectXML.class);
        if (projectXML == null) {
            return;
        }

        MenuItem add = new MenuItem("Add");
        contextMenu.getItems().add(add);
        add.setOnAction(event -> {
            ProjectLibraryXML library = new ProjectLibraryXML();
            library.setGroupId("groupId");
            library.setArtifactId("artifactId");
            library.setVersion("1.0");
            projectXML.getLibraries().add(library);
            TreeItem<EditItem> item = getTreeItem(ItemType.Library, library);
            treeItem.getChildren().add(item);
        });
    }

    private void populateParameterMenu(UI ui, ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            JobXML jobXML = (JobXML) treeItem.getParent().getValue().payload;
            JobsUIValidationResult validationResult = jobXML.removeParameter((ParameterXML) treeItem.getValue().payload);

            if (validationResult.isValid()) {
                treeItem.getParent().getChildren().remove(treeItem);
            } else {
                ui.showMessage(String.join("\n", validationResult.getMessages()));
            }
        });

        MenuItem copy = new MenuItem("Copy");
        contextMenu.getItems().add(copy);
        copy.setOnAction(t -> {
            JobXMLImpl jobXML = findAncestorPayload(treeItem, JobXMLImpl.class);
            if (jobXML == null) {
                return;
            }
            ParameterXML parameter = (ParameterXML) treeItem.getValue().payload;
            ParameterXML copied = parameter.copy();

            try {
                jobXML.add(copied);
                addParameter(treeItem.getParent(), treeItem.getValue().itemType, copied, jobXML);
            } catch (Exception e1) {
                ui.showError("Error adding new parameter.", e1);
            }
        });
    }

    private static void populateDependencyMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            ParameterXML parameterXML = (ParameterXML) treeItem.getParent().getValue().payload;
            treeItem.getParent().getChildren().remove(treeItem);
            parameterXML.removeDependency(((ParameterXML)treeItem.getValue().payload).getKey());
        });
    }

    private void populateDependenciesMenu(UI ui, ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ParameterXML parameterXML = (ParameterXML) treeItem.getValue().payload;
        List<String> dependencies = parameterXML.getDependencies();
        JobXML jobXML = findAncestorPayload(treeItem, JobXML.class);

        if (jobXML == null) {
            ui.showMessage("Cannot find job for " + treeItem.getValue());
            return;
        }

        List<String> parameters = JobXML.getAllParametersKeys(jobXML);
        parameters.removeAll(dependencies);

        if (!parameters.isEmpty()) {
            Menu addDependency = new Menu("Add");

            for (String dependency : parameters) {
                if (parameterXML.getKey().equals(dependency)) {
                    continue;
                }
                ParameterXML parameter = jobXML.getParameter(dependency);
                String name = parameter.getName();
                MenuItem dependencyMenuItem = new MenuItem(name);
                dependencyMenuItem.setOnAction(e -> {
                    parameterXML.addDependency(dependency);
                    TreeItem<EditItem> newDep = getTreeItem(ItemType.Dependency, parameter);
                    treeItem.getChildren().add(newDep);
                });
                addDependency.getItems().add(dependencyMenuItem);
            }

            contextMenu.getItems().add(addDependency);
        }
    }

    private void populateScriptsLocationMenu(UI ui, ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXML projectXML = findAncestorPayload(treeItem, ProjectFSXML.class);
        if (projectXML == null) {
            return;
        }

        MenuItem add = new MenuItem("Add");
        contextMenu.getItems().add(add);
        add.setOnAction(event -> {
            try {
                Optional<String> oname = ui.askString("File name");
                oname.ifPresent(name -> {
                    projectXML.addScriptFile(projectXML.getScriptsLocations().iterator().next(), name, "");
                    TreeItem<EditItem> scriptItem = getTreeItem(ItemType.ScriptFile, name);
                    treeItem.getChildren().add(scriptItem);
                });
            } catch (Exception e1) {
                ui.showError("Error adding new script.", e1);
            }
        });
    }

    private void populateExpressionsMenu(UI ui, ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, JobXMLImpl.class);
        if (jobXML == null) {
            return;
        }

        MenuItem add = new MenuItem("Add");
        contextMenu.getItems().add(add);
        add.setOnAction(event -> {
            String newKey = nextAvailableParameterKey(jobXML);
            ExpressionXML expressionXML = new ExpressionXML(newKey, "New expression");
            try {
                jobXML.add(expressionXML);
                addParameter(treeItem, ItemType.Expression, expressionXML, jobXML);
            } catch (Exception e1) {
                ui.showError("Error adding new expression.", e1);
            }
        });
    }

    private void populateCallsMenu(UI ui, ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, JobXMLImpl.class);
        if (jobXML == null) {
            return;
        }

        MenuItem add = new MenuItem("Add");
        contextMenu.getItems().add(add);
        add.setOnAction(event -> {
            String newKey = nextAvailableParameterKey(jobXML);
            CallXML callXML = new CallXML(newKey, "New call");
            try {
                jobXML.add(callXML);
                addParameter(treeItem, ItemType.Call, callXML, jobXML);
            } catch (Exception e1) {
                ui.showError("Error adding new call.", e1);
            }
        });
    }

    private void populateParametersMenu(UI ui, UIComponentRegistry uiComponentRegistry, ContextMenu contextMenu,
                                        TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, JobXMLImpl.class);
        if (jobXML == null) {
            return;
        }

        Menu addParameter = new Menu("Add");
        contextMenu.getItems().add(addParameter);

        uiComponentRegistry.getComponentTypes().stream()
            .sorted(Comparator.comparing(UIComponentType::getName))
            .forEach(uiComponentType -> {
                MenuItem addParameterType = new MenuItem(uiComponentType.getName());

                addParameter.getItems().add(addParameterType);

                addParameterType.setOnAction(e -> {
                    String newKey = nextAvailableParameterKey(jobXML);

                    SimpleParameterXML parameter = new SimpleParameterXML(newKey, "New Parameter");
                    parameter.setComponent(uiComponentType);
                    try {
                        jobXML.add(parameter);
                        addParameter(treeItem, ItemType.Parameter, parameter, jobXML);
                    } catch (Exception e1) {
                        ui.showError("Error adding new parameter.", e1);
                    }
                });
            });
    }

    private static String nextAvailableParameterKey(JobXML jobXML) {
        return nextAvailableKey(jobXML.getAllParameters(), ParameterXML::getKey, "newParam");
    }

    private static <T> String nextAvailableKey(Collection<T> collection, Function<T, String> getKey, String prefix) {
        String newKey = prefix;
        int i = 0;

        while (true) {
            boolean found = collection.stream()
                    .map(getKey)
                    .anyMatch(newKey::equals);
            if (!found) {
                break;
            }
            newKey = prefix + ++i;
        }
        return newKey;
    }


    private static <T> T findAncestorPayload(TreeItem<EditItem> treeItem, Class<T> payloadClass) {
        return findAncestorPayload(treeItem, item -> payloadClass.isAssignableFrom(item.payload.getClass()));
    }

    static <T> T findAncestorPayload(TreeItem<EditItem> treeItem, ItemType... itemTypes) {
        Set<ItemType> itemTypesSet = new HashSet<>(Arrays.asList(itemTypes));
        return findAncestorPayload(treeItem, item -> itemTypesSet.contains(item.itemType));
    }

    private static <T> T findAncestorPayload(TreeItem<EditItem> treeItem, Predicate<EditItem> predicate) {
        TreeItem<EditItem> ancestor = treeItem.getParent();

        if (ancestor == null) {
            return null;
        } else if (predicate.test(ancestor.getValue())) {
            return (T) ancestor.getValue().payload;
        } else {
            return findAncestorPayload(ancestor, predicate);
        }
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

    private void validate() {
        traverse(it -> {
            validate(it, false);
        });
    }
}