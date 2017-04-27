package org.jobsui.ui.javafx.edit;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.fxmisc.richtext.CodeArea;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.ui.UI;
import org.jobsui.core.xml.*;
import org.jobsui.ui.javafx.JavaFXUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by enrico on 10/9/16.
 */
public class EditProject {
    private static final Border CODE_AREA_DARK_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("039ED3"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    private static final Border CODE_AREA_DARK_NOT_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    private TreeView<Item> itemsTree;
    private VBox itemDetail;
    private ProjectFSXML projectXML = null;
    private List<String> originalJobs = null;
    private List<String> originalScriptLocations = null;
    private Button saveButton;
    private JobsUIPreferences preferences;
    private JavaFXUI ui;

    public Parent getRoot(JavaFXUI ui) throws Exception {
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
                    setprojectXML(projectXML);
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
                    newValue.getValue().onSelect();
                } catch (Throwable e) {
                    ui.showError("Error :", e);
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();

        itemsTree.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                TreeItem<Item> selected = itemsTree.getSelectionModel().getSelectedItem();

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

        itemDetail = new VBox(5);
        itemDetail.setPadding(new Insets(5, 5, 5, 5));

        SplitPane splitPane = new SplitPane(itemsTree, itemDetail);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        root.getChildren().add(splitPane);

        Label status = new Label();
        root.getChildren().add(status);

        return root;
//        Scene scene = new Scene(root, 800, 600);
//
//        stage.setScene(scene);
//        stage.show();
//        this.stage = stage;
    }

//    private static void showError(String title, Throwable e) {
//        // TODO
//        e.printStackTrace();
//        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
//        alert.setTitle(title);
//        alert.setWidth(400);
//        alert.setResizable(true);
//        alert.showAndWait();
//    }
//
//    private static void showError(String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR, message);
//        alert.setWidth(400);
//        alert.setResizable(true);
//        alert.showAndWait();
//    }

    private TreeItem<Item> loadProject(ProjectFSXML projectXML) {
        TreeItem<Item> root = new TreeItem<>(new Item(ItemType.Project, projectXML::getName, projectXML));

        TreeItem<Item> libraries = new TreeItem<>(new Item(ItemType.Libraries, () -> "libraries", projectXML));
        root.getChildren().add(libraries);
        projectXML.getLibraries().stream()
                .map(l -> new Item(ItemType.Library, () -> l, l))
                .map(TreeItem::new)
                .forEach(treeItem -> libraries.getChildren().add(treeItem));

        for (String location : projectXML.getScriptsLocations()) {
            TreeItem<Item> locationItem = new TreeItem<>(new Item(ItemType.Scripts, () -> location, projectXML));
            root.getChildren().add(locationItem);
            projectXML.getScriptFiles(location).entrySet().stream()
                    .map(e -> new Item(ItemType.ScriptFile, e::getKey, e))
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

    private TreeItem<Item> loadProject(File file) throws Exception {
        //TODO
        return null;
//        ProjectParser parser = ProjectParser.getParser(file.getAbsolutePath());
//        return loadProject(parser.parse());
    }

    private TreeItem<Item> createJobTreeItem(JobXML job) {
        TreeItem<Item> result = new TreeItem<>(new Item(ItemType.Job, job::getName, job));

        addParameters(result, job, "parameters", ItemType.Parameter, job.getSimpleParameterXMLs());
        addParameters(result, job, "expressions", ItemType.Expression, job.getExpressionXMLs());
        addParameters(result, job, "calls", ItemType.Call, job.getCallXMLs());

        result.setExpanded(true);
        return result;
    }

    private void addParameters(TreeItem<Item> result, JobXML jobXML, String containerText, ItemType itemType,
                               List<? extends ParameterXML> parametersList) {
        TreeItem<Item> parameters = new TreeItem<>(new Item(ItemType.Parameters, () -> containerText, jobXML));
        parameters.setExpanded(true);
        result.getChildren().add(parameters);

        parametersList.forEach(parameter -> addParameter(parameters, itemType, parameter, jobXML));
    }

    private void addParameter(TreeItem<Item> parameters, ItemType itemType, ParameterXML parameterDef, JobXML jobXML) {
        TreeItem<Item> parameterTI = new TreeItem<>(new Item(itemType, parameterDef::getName, parameterDef));
        parameters.getChildren().add(parameterTI);
        TreeItem<Item> dependencies = new TreeItem<>(new Item(ItemType.Dependencies, () -> "dependencies", parameterDef));
        parameterTI.getChildren().add(dependencies);
        parameterDef.getDependencies().stream()
                .map(depKey -> {
                    ParameterXML dep = jobXML.getParameter(depKey);
                    if (dep == null) {
                        throw new RuntimeException("Cannot find parameter for dependency '" + depKey + "'.");
                    }
                    return dep;
                })
                .map(dep -> new Item(ItemType.Dependency, dep::getName, dep.getKey()))
                .map(TreeItem::new)
                .forEach(dependencies.getChildren()::add);
    }

    public void edit(ProjectFSXML projectXML, boolean isNew) {
        setprojectXML(projectXML);

        if (isNew) {
            saveButton.setDisable(true);
        }

        TreeItem<Item> root = loadProject(projectXML);

        Platform.runLater(() -> {
            itemsTree.setRoot(root);
            root.setExpanded(true);
        });
    }

    private void setprojectXML(ProjectFSXML projectXML) {
        this.projectXML = projectXML;
        this.originalJobs = new ArrayList<>(projectXML.getJobs());
        this.originalScriptLocations = new ArrayList<>(projectXML.getScriptsLocations());
//        stage.setTitle(projectXML.getName());
    }

    private enum ItemType {
        Project, ScriptFile, Job, Parameter, Expression, Dependency, Dependencies, Parameters, Scripts, Libraries, Library, Call
    }

    private class Item {
        private final ItemType itemType;
        private final Supplier<String> title;
        private final Object payload;

        Item(ItemType itemType, Supplier<String> title, Object payload) {
            this.itemType = itemType;
            this.title = title;
            this.payload = payload;
        }

        void onSelect() throws IOException {
            itemDetail.getChildren().clear();

            switch (itemType) {
                case Project:
                    setProjectDetail();
                    break;

                case ScriptFile:
                    setGroovyFileDetail();
                    break;

                case Job:
                    setJobDetail();
                    break;

                case Parameter:
                    setParameterDetail();
                    break;

                case Expression: {
                    setExpressionDetail();
                    break;
                }

                case Call: {
                    setCallDetail();
                    break;
                }
            }
        }

        private void setProjectDetail() {
            ProjectFSXML project = (ProjectFSXML) payload;

            addTextProperty("Name:", project::getName, project::setName);
            addTextProperty("Version:", project::getVersion, project::setVersion);
        }

        private void setJobDetail() {
            JobXMLImpl jobXML = (JobXMLImpl) payload;

            // TODO key (file name)
            addTextProperty("Name:", jobXML::getName, jobXML::setName);

            addTextAreaProperty("Validate:", jobXML::getValidateScript, jobXML::setValidateScript,
                    false);
            addTextAreaProperty("Run:", jobXML::getRunScript, jobXML::setRunScript, false);
        }

        private void setGroovyFileDetail() throws IOException {
            String content = (String) payload;
//            if (file.getName().endsWith(".groovy") ||
//                    file.getName().endsWith(".txt") ||
//                    file.getName().endsWith(".properties") ||
//                    file.getName().endsWith(".xml")) {
//                String content = new String(Files.readAllBytes(file.toPath()));
                itemDetail.getChildren().add(new Label("Content:"));

                CodeArea codeArea = GroovyCodeArea.getCodeArea(true, preferences.getTheme());
                GroovyCodeArea.setText(codeArea, content);
                GroovyCodeArea.resetCaret(codeArea);

                VBox.setVgrow(codeArea, Priority.ALWAYS);

                itemDetail.getChildren().add(codeArea);
//            }
        }

        private void setCallDetail() {
            CallXML parameter = (CallXML) payload;

            addTextProperty("Key:", parameter::getKey, parameter::setKey);
            addTextProperty("Name:", parameter::getName, parameter::setName);

            // TODO
        }

        private void setExpressionDetail() {
            ExpressionXML parameter = (ExpressionXML) payload;

            addTextProperty("Key:", parameter::getKey, parameter::setKey);
            addTextProperty("Name:", parameter::getName, parameter::setName);

            addTextAreaProperty("Evaluate:", parameter::getEvaluateScript, parameter::setEvaluateScript,
                    false);
        }

        private void setParameterDetail() {
            TreeItem<Item> treeItem = itemsTree.getSelectionModel().getSelectedItem();

            if (treeItem == null) {
                ui.showMessage("Cannot find item \"" + payload + "\".");
                return;
            }

            JobXMLImpl jobXML = findAncestorPayload(treeItem, ItemType.Job);

            if (jobXML == null) {
                ui.showMessage("Cannot find job for item \"" + payload + "\".");
                return;
            }

            SimpleParameterXML parameter = (SimpleParameterXML) payload;

            addTextProperty("Key:", parameter::getKey, key -> jobXML.changeParameterKey(parameter, key));

            addTextProperty("Name:", parameter::getName, parameter::setName);

            addTextAreaProperty("On init:", parameter::getOnInitScript,
                    parameter::setOnInitScript, false);

            addTextAreaProperty("On dependencies change:", parameter::getOnDependenciesChangeScript,
                    parameter::setOnDependenciesChangeScript, false);

            addTextAreaProperty("Validate:", parameter::getValidateScript, parameter::setValidateScript,
                    false);
        }

        private void addTextAreaProperty(String title, Supplier<String> get, Consumer<String> set, boolean showLineNumbers) {
            itemDetail.getChildren().add(new Label(title));
            VBox parent = new VBox();
            CodeArea codeArea = GroovyCodeArea.getCodeArea(true, preferences.getTheme());

            String content = get.get();
            if (content != null) {
                GroovyCodeArea.setText(codeArea, content);
                GroovyCodeArea.resetCaret(codeArea);
            }

            codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
                set.accept(newValue);
                updateSelectedItem();
            });

            itemDetail.getChildren().add(parent);

            parent.getChildren().add(codeArea);
            if (preferences.getTheme().equals(JobsUITheme.Dark)) {
                parent.setBorder(CODE_AREA_DARK_NOT_FOCUSED_BORDER);
                codeArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        parent.setBorder(CODE_AREA_DARK_FOCUSED_BORDER);
                    } else {
                        parent.setBorder(CODE_AREA_DARK_NOT_FOCUSED_BORDER);
                    }
                });
            }

            VBox.setVgrow(parent, Priority.ALWAYS);
        }

        private void addTextProperty(String title, Supplier<String> get, Consumer<String> set) {
            itemDetail.getChildren().add(new Label(title));
            TextField control = new TextField(get.get());

            control.textProperty().addListener((observable, oldValue, newValue) -> {
                set.accept(newValue);
                updateSelectedItem();
            });
            itemDetail.getChildren().add(control);
        }

        @Override
        public String toString() {
            return title.get();
        }
    }

    private void updateSelectedItem() {
        TreeItem<Item> selectedItem = itemsTree.getSelectionModel().getSelectedItem();

        Object payload = selectedItem.getValue().payload;
        if (payload instanceof  ValidatingXML) {
            validate(selectedItem, (ValidatingXML) payload);
        }

        Item value = selectedItem.getValue();
        selectedItem.setValue(null);
        selectedItem.setValue(value);
    }


    private void validate(TreeItem<Item> treeItem, ValidatingXML validatingXML) {
        List<String> validate = validatingXML.validate();
        if (!validate.isEmpty()) {
            Label label = new Label("?");
            label.setTextFill(Color.RED);
            label.setTooltip(new Tooltip(String.join(", ", validate)));
            treeItem.setGraphic(label);
        } else {
            treeItem.setGraphic(null);
        }
    }

    /*
    private final class TextFieldTreeCellImpl extends TreeCell<Item> {

        TextFieldTreeCellImpl() {
        }

        @Override
        protected void updateItem(Item item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item != null) {
                setText(item.title.get());
                setGraphic(getTreeItem().getGraphic());
            }
        }
    }
    */

    private void populateContextMenu(ContextMenu contextMenu, TreeItem<Item> treeItem) {
        contextMenu.getItems().clear();
        Item item = treeItem.getValue();

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
                        TreeItem<Item> newDep = new TreeItem<>(EditProject.this.new Item(ItemType.Dependency,
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

    private void addParameterMenu(ContextMenu contextMenu, TreeItem<Item> treeItem) {
        JobXMLImpl jobXML = findAncestorPayload(treeItem, ItemType.Job);
        if (jobXML == null) {
            return;
        }
        MenuItem addParameter = new MenuItem("Add parameter");
        contextMenu.getItems().add(addParameter);
        addParameter.setOnAction(e -> {
            SimpleParameterXML parameter = new SimpleParameterXML("newKey", "newName");
            try {
                jobXML.add(parameter);
                addParameter(treeItem, ItemType.Parameter, parameter, jobXML);
            } catch (Exception e1) {
                ui.showError("Error adding new parameter.", e1);
            }
        });
    }

    private <T> T findAncestorPayload(TreeItem<Item> treeItem, ItemType... itemTypes) {
        TreeItem<Item> ancestor = treeItem.getParent();
        while (ancestor != null) {
            Item value = ancestor.getValue();
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

//    private class OpenProjectTask extends Task {
//        private final File file;
//
//        OpenProjectTask(File file) {
//            this.file = file;
//        }
//
//        @Override
//        protected Void call() throws Exception {
//            Platform.runLater(() -> {
//                root.setDisable(true);
//                itemDetail.getChildren().clear();
//                itemsTree.setRoot(null);
//                status.setText("Loading project ...");
//            });
//            try {
//                TreeItem<Item> root = loadProject(file);
//
//                configuration.addRecentProject(file);
//                EditProjectConfiguration.save(configuration);
//
//                Platform.runLater(() -> {
//                    itemsTree.setRoot(root);
//                    root.setExpanded(true);
//                });
//            } catch (Exception e) {
//                JavaFXUI.showErrorStatic("Error loading project.", e);
//            } finally {
//                Platform.runLater(() -> {
//                    status.setText("");
//                    root.setDisable(false);
//                });
//            }
//            return null;
//        }
//    }

    private TreeItem<Item> findItem(TreeItem<Item> root, Item item) {
        if (root.getValue() == item) {
            return root;
        }
        for (TreeItem<Item> child : root.getChildren()) {
            TreeItem<Item> found = findItem(child, item);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}