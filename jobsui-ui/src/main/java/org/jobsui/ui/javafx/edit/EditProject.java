package org.jobsui.ui.javafx.edit;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import org.jobsui.core.runner.JobsUIValidationResult;
import org.jobsui.core.ui.*;
import org.jobsui.core.utils.JobsUIUtils;
import org.jobsui.core.xml.*;
import org.jobsui.ui.javafx.JavaFXUI;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/9/16.
 */
public class EditProject {
    private TreeView<EditItem> itemsTree;
    private ItemDetail itemDetail;
    private ProjectFSXML projectXML = null;
    private List<JobXML> originalJobs = null;
    private List<String> originalScriptLocations = null;
    private UIButton<Node> saveButton;
    private JobsUIPreferences preferences;
    private JavaFXUI ui;
    private SplitPane splitPane;
    private final UIComponentRegistryComposite uiComponentRegistry = new UIComponentRegistryComposite();
    /**
     * The idea is that new types of UIComponent, or different implementations, can be created by users.
     * TODO find a way to customize it
     */
    private final UIComponentRegistry customUiComponentRegistry = new UIComponentRegistry() {
        @Override
        public Optional<UIComponentType> getComponentType(String name) {
            return Optional.empty();
        }

        @Override
        public Collection<UIComponentType> getComponentTypes() {
            return Collections.emptyList();
        }
    };

    public EditProject() {
        uiComponentRegistry.add(customUiComponentRegistry);
        uiComponentRegistry.add(new UIComponentRegistryImpl());
    }

    public Parent getEditNode(JavaFXUI ui) {
        this.ui = ui;
        preferences = ui.getPreferences();
        VBox root = new VBox(5);
        HBox buttons = new HBox(5);
        VBox.setVgrow(buttons, Priority.NEVER);
        buttons.setPadding(new Insets(5, 5, 5, 5));

        saveButton = ui.createButton();
        saveButton.setTitle("Save");
        saveButton.getObservable().subscribe(event -> {
            // I backup the current project
            File tempDir;
            try {
                tempDir = JobsUIUtils.createTempDir("jobsui", projectXML.getFolder().getName());
                FileUtils.copyDirectory(projectXML.getFolder(), tempDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                for (JobXML originalJob : originalJobs) {
                    File file = new File(projectXML.getFolder(), JobXMLImpl.getFileName(originalJob.getId()));
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

        itemDetail = new ItemDetail(ui, uiComponentRegistry);
        itemDetail.setPadding(new Insets(5, 5, 5, 5));

        splitPane = new SplitPane(itemsTree, itemDetail);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        root.getChildren().add(splitPane);

        Label status = new Label();
        root.getChildren().add(status);

        return root;
    }

    private TreeItem<EditItem> loadProject(ProjectFSXML projectXML) {
        TreeItem<EditItem> root = new TreeItem<>(new EditItem(ItemType.Project, projectXML));

        TreeItem<EditItem> libraries = new TreeItem<>(new EditItem(ItemType.Libraries, projectXML));
        root.getChildren().add(libraries);
        projectXML.getLibraries().stream()
                .map(l -> new EditItem(ItemType.Library, l))
                .map(TreeItem::new)
                .forEach(treeItem -> libraries.getChildren().add(treeItem));

        TreeItem<EditItem> scriptsItem = new TreeItem<>(new EditItem(ItemType.Scripts, projectXML));
        root.getChildren().add(scriptsItem);
        for (String location : projectXML.getScriptsLocations()) {
            TreeItem<EditItem> locationItem = new TreeItem<>(new EditItem(ItemType.ScriptsLocation, location));
            scriptsItem.getChildren().add(locationItem);
            projectXML.getScriptFilesNames(location).stream()
                    .map(fileName -> new EditItem(ItemType.ScriptFile, fileName))
                    .map(TreeItem::new)
                    .forEach(treeItem -> locationItem.getChildren().add(treeItem));
        }

        projectXML.getJobs().stream()
                .sorted(Comparator.comparing(JobXML::getName))
                .map(this::createJobTreeItem)
                .forEach(root.getChildren()::add);
        return root;
    }

    private TreeItem<EditItem> createJobTreeItem(JobXML job) {
        TreeItem<EditItem> result = new TreeItem<>(new EditItem(ItemType.Job, job));


        addParameters(result, job, ItemType.Parameters, ItemType.Parameter, job.getSimpleParameterXMLs());
        addParameters(result, job, ItemType.Expressions, ItemType.Expression, job.getExpressionXMLs());
        addParameters(result, job, ItemType.Calls, ItemType.Call, job.getCallXMLs());

        addWizard(job, result);

        result.setExpanded(true);
        return result;
    }

    private void addWizard(JobXML job, TreeItem<EditItem> result) {
        TreeItem<EditItem> wizardSteps = new TreeItem<>(new EditItem(ItemType.WizardSteps, job));

        result.getChildren().add(wizardSteps);

        job.getWizardSteps().forEach(w -> {
            TreeItem<EditItem> wizardStep = new TreeItem<>(new EditItem(ItemType.WizardStep, w));
            wizardSteps.getChildren().add(wizardStep);
            TreeItem<EditItem> dependencies = new TreeItem<>(new EditItem(ItemType.WizardStepDependencies, w));
            wizardStep.getChildren().add(dependencies);

            w.getDependencies().forEach(d -> {
                TreeItem<EditItem> dependency = new TreeItem<>(new EditItem(ItemType.WizardStepDependency, job.getParameter(d)));
                dependencies.getChildren().add(dependency);
            });

        });

    }

    private void addParameters(TreeItem<EditItem> result, JobXML jobXML, ItemType containerType, ItemType itemType,
                               List<? extends ParameterXML> parametersList) {
        TreeItem<EditItem> parameters = new TreeItem<>(new EditItem(containerType, jobXML));
        parameters.setExpanded(true);
        result.getChildren().add(parameters);

        parametersList.forEach(parameter -> addParameter(parameters, itemType, parameter, jobXML));
    }

    private void addParameter(TreeItem<EditItem> parameters, ItemType itemType, ParameterXML parameterDef, JobXML jobXML) {
        TreeItem<EditItem> parameterTI = new TreeItem<>(new EditItem(itemType, parameterDef));
        parameters.getChildren().add(parameterTI);
        TreeItem<EditItem> dependencies = new TreeItem<>(new EditItem(ItemType.Dependencies, parameterDef));
        parameterTI.getChildren().add(dependencies);
        parameterDef.getDependencies().stream()
                .map(depKey -> {
                    ParameterXML dep = jobXML.getParameter(depKey);
                    if (dep == null) {
                        throw new RuntimeException("Cannot find parameter for dependency '" + depKey + "'.");
                    }
                    return dep;
                })
                .map(dep -> new EditItem(ItemType.Dependency, dep))
                .map(TreeItem::new)
                .forEach(dependencies.getChildren()::add);
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

    private void populateContextMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        contextMenu.getItems().clear();
        EditItem item = treeItem.getValue();

        if (item.itemType == ItemType.Parameters) {
            populateParametersMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Parameter) {
            populateParameterMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Dependencies) {
            populateDependenciesMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Dependency) {
            populateDependencyMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Expressions) {
            populateExpressionsMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Expression) {
            populateParameterMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Calls) {
            populateCallsMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.Call) {
            populateParameterMenu(contextMenu, treeItem);
        } else if (item.itemType == ItemType.ScriptsLocation) {
            populateScriptsLocationMenu(contextMenu, treeItem);
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
        }
    }

    private void populateProjectMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXMLImpl projectXML = (ProjectFSXMLImpl) treeItem.getValue().payload;

        MenuItem add = new MenuItem("Add");
        contextMenu.getItems().add(add);
        add.setOnAction(t -> {
            JobXML jobXML;
            try {
                jobXML = NewProject.createJobXML();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            projectXML.addJob(jobXML);

            TreeItem<EditItem> item = createJobTreeItem(jobXML);

            treeItem.getChildren().add(item);
        });

    }

    private void populateScriptFileMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXML projectXML = findAncestorPayload(treeItem, ItemType.Project);

        String location = findAncestorPayload(treeItem, ItemType.ScriptsLocation);

        String scriptFile = (String) treeItem.getValue().payload;

        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            projectXML.removeScriptFile(location, scriptFile);
            treeItem.getParent().getChildren().remove(treeItem);
        });

    }

    private void populateWizardStepDependencyMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        WizardStep wizardStep = findAncestorPayload(treeItem, ItemType.WizardStep);

        String parameterKey = ((ParameterXML) treeItem.getValue().payload).getKey();

        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            wizardStep.getDependencies().remove(parameterKey);
            treeItem.getParent().getChildren().remove(treeItem);
        });
    }

    private void populateWizardStepDependenciesMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXML jobXML = findAncestorPayload(treeItem, ItemType.Job);

        WizardStep wizardStep = (WizardStep) treeItem.getValue().payload;

        List<String> parameters = getParametersKeys(jobXML);
        parameters.removeAll(wizardStep.getDependencies());

        if (!parameters.isEmpty()) {
            Menu addDependency = new Menu("Add");

            for (String dependency : parameters) {

                ParameterXML parameter = jobXML.getParameter(dependency);
                String name = parameter.getName();
                MenuItem dependencyMenuItem = new MenuItem(name);
                dependencyMenuItem.setOnAction(e -> {
                    wizardStep.getDependencies().add(dependency);
                    TreeItem<EditItem> newDep = new TreeItem<>(new EditItem(ItemType.WizardStepDependency,
                            parameter));
                    treeItem.getChildren().add(newDep);
                });
                addDependency.getItems().add(dependencyMenuItem);
            }

            contextMenu.getItems().add(addDependency);
        }

    }

    private void populateWizardStepMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXML jobXML = findAncestorPayload(treeItem, ItemType.Job);
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
        JobXML jobXML = findAncestorPayload(treeItem, ItemType.Job);
        if (jobXML == null) {
            return;
        }

        MenuItem add = new MenuItem("Add");
        contextMenu.getItems().add(add);
        add.setOnAction(t -> {
            WizardStepImpl wizardStep = new WizardStepImpl();
            wizardStep.setName("New wizard step");
            jobXML.getWizardSteps().add(wizardStep);
            TreeItem<EditItem> item = new TreeItem<>(new EditItem(ItemType.WizardStep, wizardStep));
            TreeItem<EditItem> dependencies = new TreeItem<>(new EditItem(ItemType.WizardStepDependencies, wizardStep));
            item.getChildren().add(dependencies);

            treeItem.getChildren().add(item);
        });
    }

    private void populateLibraryMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXMLImpl projectXML = findAncestorPayload(treeItem, ItemType.Project);
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
        ProjectFSXMLImpl projectXML = findAncestorPayload(treeItem, ItemType.Project);
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
            TreeItem<EditItem> item = new TreeItem<>(new EditItem(ItemType.Library, library));
            treeItem.getChildren().add(item);
        });
    }

    private void populateParameterMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            JobXML jobXML = (JobXML) treeItem.getParent().getValue().payload;
            JobsUIValidationResult validationResult = jobXML.removeParameter((ParameterXML) treeItem.getValue().payload);

            if (validationResult.isValid()) {
                treeItem.getParent().getChildren().remove(treeItem);
            } else {
                ui.showMessage(validationResult.getMessages().stream().collect(Collectors.joining("\n")));
            }
        });
    }

    private void populateDependencyMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().add(delete);
        delete.setOnAction(t -> {
            ParameterXML parameterXML = (ParameterXML) treeItem.getParent().getValue().payload;
            treeItem.getParent().getChildren().remove(treeItem);
            parameterXML.removeDependency(((ParameterXML)treeItem.getValue().payload).getKey());
        });
    }

    private void populateDependenciesMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ParameterXML parameterXML = (ParameterXML) treeItem.getValue().payload;
        List<String> dependencies = parameterXML.getDependencies();
        JobXML jobXML = findAncestorPayload(treeItem, ItemType.Job);

        if (jobXML == null) {
            ui.showMessage("Cannot find job for " + treeItem.getValue());
            return;
        }

        List<String> parameters = getAllParametersKeys(jobXML);
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
                    TreeItem<EditItem> newDep = new TreeItem<>(new EditItem(ItemType.Dependency,
                            parameter));
                    treeItem.getChildren().add(newDep);
                });
                addDependency.getItems().add(dependencyMenuItem);
            }

            contextMenu.getItems().add(addDependency);
        }
    }

    private void populateScriptsLocationMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        ProjectFSXML projectXML = findAncestorPayload(treeItem, ItemType.Project);
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
                    TreeItem<EditItem> scriptItem = new TreeItem<>(new EditItem(ItemType.ScriptFile, name));
                    treeItem.getChildren().add(scriptItem);
                });
            } catch (Exception e1) {
                ui.showError("Error adding new script.", e1);
            }
        });
    }

    private void populateExpressionsMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, ItemType.Job);
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

    private void populateCallsMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, ItemType.Job);
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

    private void populateParametersMenu(ContextMenu contextMenu, TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, ItemType.Job);
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

    private String nextAvailableParameterKey(JobXML jobXML) {
        String newKey = "newKey";
        int i = 0;

        while (true) {
            boolean found = jobXML.getAllParameters().stream()
                    .map(ParameterXML::getKey)
                    .anyMatch(newKey::equals);
            if (!found) {
                break;
            }
            newKey = "newKey" + Integer.toString(++i);
        }
        return newKey;
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

    private static List<String> getAllParametersKeys(JobXML jobXML) {
        return jobXML.getAllParameters().stream()
                .map(ParameterXML::getKey).collect(Collectors.toList());
    }

    private static List<String> getParametersKeys(JobXML jobXML) {
        return jobXML.getSimpleParameterXMLs().stream()
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