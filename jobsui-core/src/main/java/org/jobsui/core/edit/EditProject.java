package org.jobsui.core.edit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.utils.JobsUIUtils;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by enrico on 10/9/16.
 */
public class EditProject extends Application {
    private EditProjectConfiguration configuration;
    private TreeView<Item> itemsTree;
    private VBox itemDetail;
    private VBox root;
    private Label status;
    private ProjectXML projectXML = null;

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        configuration = EditProjectConfiguration.load();
        root = new VBox(5);
        HBox buttons = new HBox(5);
        VBox.setVgrow(buttons, Priority.NEVER);
        buttons.setPadding(new Insets(5, 5, 5, 5));
        Button openButton = new Button("Open");
        openButton.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Open project");

            File firstProject = configuration.getFirstRecentValidProject();
            if (firstProject != null) {
                chooser.setInitialDirectory(firstProject);
            }

            File file = chooser.showDialog(stage);

            if (file != null) {
                Task runnable = new OpenProjectTask(file);

                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.start();
            }
        });
        buttons.getChildren().add(openButton);

        Button export = new Button("Export");
        export.setOnAction(event -> {
            try {
                projectXML.export();
            } catch (Exception e) {
                JavaFXUI.showErrorStatic("Error exporting project.", e);
            }
        });
        buttons.getChildren().add(export);

        root.getChildren().add(buttons);

        itemsTree = new TreeView<>();
//        itemsTree.setCellFactory(param -> new TextFieldTreeCellImpl());
        itemsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    newValue.getValue().onSelect();
                } catch (Throwable e) {
                    JavaFXUI.showErrorStatic("Error :", e);
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

        status = new Label();
        root.getChildren().add(status);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Edit JobsUI");
        stage.setScene(scene);
        stage.show();
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

    private TreeItem<Item> loadProject(File file) throws Exception {
        JobParser parser = new JobParser();
        projectXML = parser.loadProject(file);
        TreeItem<Item> root = new TreeItem<>(new Item(ItemType.Project, projectXML::getName, projectXML));

        TreeItem<Item> libraries = new TreeItem<>(new Item(ItemType.Libraries, () -> "libraries", projectXML));
        root.getChildren().add(libraries);
        projectXML.getLibraries().stream()
                .map(l -> new Item(ItemType.Library, () -> l, l))
                .map(TreeItem::new)
                .forEach(treeItem -> libraries.getChildren().add(treeItem));

        TreeItem<Item> groovy = new TreeItem<>(new Item(ItemType.Groovy, () -> "groovy", projectXML));
        root.getChildren().add(groovy);
        projectXML.getGroovyFiles().stream()
                .map(f -> new Item(ItemType.GroovyFile, f::getName, f))
                .map(TreeItem::new)
                .forEach(treeItem -> groovy.getChildren().add(treeItem));

        projectXML.getJobs().values().stream()
                .sorted(Comparator.comparing(JobXML::getName))
                .map(this::createJobTreeItem)
                .forEach(root.getChildren()::add);
        return root;
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
                .map(jobXML::getParameter)
                .map(dep -> new Item(ItemType.Dependency, dep::getName, dep.getKey()))
                .map(TreeItem::new)
                .forEach(dependencies.getChildren()::add);
    }

    private enum ItemType {
        Project, GroovyFile, Job, Parameter, Expression, Dependency, Dependencies, Parameters, Groovy, Libraries, Library, Call
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

                case GroovyFile:
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
            ProjectXML project = (ProjectXML) payload;

            addTextProperty("Name:", project::getName, project::setName);
        }

        private void setJobDetail() {
            JobXML jobXML = (JobXML) payload;

            // TODO key (file name)
            addTextProperty("Name:", jobXML::getName, jobXML::setName);

            addTextAreaProperty("Validate:", jobXML::getValidateScript, jobXML::setValidateScript);
            addTextAreaProperty("Run:", jobXML::getRunScript, jobXML::setRunScript);
        }

        private void setGroovyFileDetail() throws IOException {
            File file = (File) payload;
            if (file.getName().endsWith(".groovy") ||
                    file.getName().endsWith(".txt") ||
                    file.getName().endsWith(".properties") ||
                    file.getName().endsWith(".xml")) {
                String content = new String(Files.readAllBytes(file.toPath()));
                itemDetail.getChildren().add(new Label("Content:"));
                TextArea textArea = new TextArea(content);
                VBox.setVgrow(textArea, Priority.ALWAYS);
                itemDetail.getChildren().add(textArea);
            }
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

            TextArea textArea = addTextAreaProperty("Evaluate:", parameter::getEvaluateScript, parameter::setEvaluateScript);
            VBox.setVgrow(textArea, Priority.ALWAYS);
        }

        private void setParameterDetail() {
            TreeItem<Item> treeItem = itemsTree.getSelectionModel().getSelectedItem();

            if (treeItem == null) {
                JavaFXUI.showMessageStatic("Cannot find item \"" + payload + "\".");
                return;
            }

            JobXML jobXML = findAncestorPayload(treeItem, ItemType.Job);

            if (jobXML == null) {
                JavaFXUI.showMessageStatic("Cannot find job for item \"" + payload + "\".");
                return;
            }

            SimpleParameterXML parameter = (SimpleParameterXML) payload;

            addTextProperty("Key:", parameter::getKey, key -> jobXML.changeParameterKey(parameter, key));

            addTextProperty("Name:", parameter::getName, parameter::setName);

            addTextAreaProperty("Create component:", parameter::getCreateComponentScript,
                    parameter::setCreateComponentScript);

            addTextAreaProperty("On dependencies change:", parameter::getOnDependenciesChangeScript,
                    parameter::setOnDependenciesChangeScript);

            addTextAreaProperty("Validate:", parameter::getValidateScript, parameter::setValidateScript);
        }

        private TextArea addTextAreaProperty(String title, Supplier<String> get, Consumer<String> set) {
            itemDetail.getChildren().add(new Label(title));
            TextArea control = new TextArea(get.get());

            control.textProperty().addListener((observable, oldValue, newValue) -> {
                set.accept(newValue);
                updateSelectedItem();
            });
            itemDetail.getChildren().add(control);
            return control;
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
            label.setTooltip(new Tooltip(JobsUIUtils.join(validate, " ")));
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
                JavaFXUI.showMessageStatic("Cannot find job for " + item);
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
        JobXML jobXML = findAncestorPayload(treeItem, ItemType.Job);
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
                JavaFXUI.showErrorStatic("Error adding new parameter.", e1);
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

    private class OpenProjectTask extends Task {
        private final File file;

        OpenProjectTask(File file) {
            this.file = file;
        }

        @Override
        protected Void call() throws Exception {
            Platform.runLater(() -> {
                root.setDisable(true);
                itemDetail.getChildren().clear();
                itemsTree.setRoot(null);
                status.setText("Loading project ...");
            });
            try {
                TreeItem<Item> root = loadProject(file);

                configuration.addRecentProject(file);
                EditProjectConfiguration.save(configuration);

                Platform.runLater(() -> {
                    itemsTree.setRoot(root);
                    root.setExpanded(true);
                });
            } catch (Exception e) {
                JavaFXUI.showErrorStatic("Error loading project.", e);
            } finally {
                Platform.runLater(() -> {
                    status.setText("");
                    root.setDisable(false);
                });
            }
            return null;
        }
    }

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