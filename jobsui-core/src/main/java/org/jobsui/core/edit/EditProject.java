package org.jobsui.core.edit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jobsui.core.groovy.*;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by enrico on 10/9/16.
 */
public class EditProject extends Application {
    private TreeView<Item> items;
    private VBox item;
    private VBox root;
    private Label status;
    private ProjectXML projectXML = null;

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        root = new VBox(5);
        HBox buttons = new HBox(5);
        buttons.setPadding(new Insets(5, 5, 5, 5));
        Button load = new Button("Load");
        load.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Load project");

            File file = chooser.showDialog(stage);

            if (file != null) {
                Task runnable = new Task() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> {
                            root.setDisable(true);
                            item.getChildren().clear();
                            items.setRoot(null);
                            status.setText("Loading project ...");
                        });
                        try {
                            TreeItem<Item> root = loadProject(file);

                            Platform.runLater(() -> {
                                items.setRoot(root);
                                root.setExpanded(true);
                            });
                        } catch (Exception e) {
                            showError("Error loading project.", e);
                        } finally {
                            Platform.runLater(() -> {
                                status.setText("");
                                root.setDisable(false);
                            });
                        }
                        return null;
                    }
                };

                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.start();
            }
        });
        buttons.getChildren().add(load);

        Button export = new Button("Export");
        export.setOnAction(event -> {
            try {
                projectXML.export();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        buttons.getChildren().add(export);

        root.getChildren().add(buttons);

        items = new TreeView<>();
        items.setCellFactory(param -> new TextFieldTreeCellImpl());
        items.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    newValue.getValue().onSelect();
                } catch (Throwable e) {
                    showError("Error :", e);
                }
            }
        });

        item = new VBox(5);
        item.setPadding(new Insets(5, 5, 5, 5));

        SplitPane splitPane = new SplitPane(items, item);
        root.getChildren().add(splitPane);

        status = new Label();
        root.getChildren().add(status);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Edit JobsUI");
        stage.setScene(scene);
        stage.show();
    }

    private static void showError(String message, Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message + " " + e.getMessage());
        alert.showAndWait();
    }

    private TreeItem<Item> loadProject(File file) throws Exception {
        JobParser parser = new JobParser();
        projectXML = parser.loadProject(file);
        TreeItem<Item> root = new TreeItem<>(new Item(ItemType.Project, projectXML.getName(), projectXML));

        TreeItem<Item> libraries = new TreeItem<>(new Item(ItemType.Libraries, "libraries", projectXML));
        root.getChildren().add(libraries);
        projectXML.getLibraries().stream()
                .map(l -> new Item(ItemType.Library, l, l))
                .map(TreeItem::new)
                .forEach(treeItem -> libraries.getChildren().add(treeItem));

        TreeItem<Item> groovy = new TreeItem<>(new Item(ItemType.Groovy, "groovy", projectXML));
        root.getChildren().add(groovy);
        projectXML.getGroovyFiles().stream()
                .map(f -> new Item(ItemType.GroovyFile, f.getName(), f))
                .map(TreeItem::new)
                .forEach(treeItem -> groovy.getChildren().add(treeItem));

        projectXML.getJobs().values().stream()
                .sorted(Comparator.comparing(JobXML::getName))
                .map(this::createJobTreeItem)
                .forEach(root.getChildren()::add);
        return root;
    }

    private TreeItem<Item> createJobTreeItem(JobXML job) {
        TreeItem<Item> result = new TreeItem<>(new Item(ItemType.Job, job.getName(), job));

        addParameters(result, job, "parameters", ItemType.Parameter, job.getSimpleParameterXMLs());
        addParameters(result, job, "expressions", ItemType.Expression, job.getExpressionXMLs());
        addParameters(result, job, "calls", ItemType.Call, job.getCallXMLs());

        result.setExpanded(true);
        return result;
    }

    private void addParameters(TreeItem<Item> result, JobXML jobXML, String containerText, ItemType itemType,
                               List<? extends ParameterXML> parametersList) {
        TreeItem<Item> parameters = new TreeItem<>(new Item(ItemType.Parameters, containerText, jobXML));
        parameters.setExpanded(true);
        result.getChildren().add(parameters);

        parametersList.stream()
                .forEach(parameter -> addParameter(parameters, itemType, parameter, jobXML));
    }

    private void addParameter(TreeItem<Item> parameters, ItemType itemType, ParameterXML parameterDef, JobXML jobXML) {
        TreeItem<Item> parameterTI = new TreeItem<>(new Item(itemType, parameterDef.getName(), parameterDef));
        parameters.getChildren().add(parameterTI);
        TreeItem<Item> dependencies = new TreeItem<>(new Item(ItemType.Dependencies, "dependencies", parameterDef));
        parameterTI.getChildren().add(dependencies);
        parameterDef.getDependencies().stream()
                .map(jobXML::getParameter)
                .map(dep -> new Item(ItemType.Dependency, dep.getName(), dep))
                .map(TreeItem::new)
                .forEach(dependencies.getChildren()::add);
    }

    private enum ItemType {
        Project, GroovyFile, Job, Parameter, Expression, Dependency, Dependencies, Parameters, Groovy, Libraries, Library, Call
    }

    private class Item {
        private final ItemType itemType;
        private final String title;
        private final Object payload;

        Item(ItemType itemType, String title, Object payload) {
            this.itemType = itemType;
            this.title = title;
            this.payload = payload;
        }

        void onSelect() throws IOException {
            item.getChildren().clear();
            switch (itemType) {
                case Project:
                    ProjectXML project = (ProjectXML) payload;
                    break;

                case GroovyFile:
                    File file = (File) payload;
                    if (file.getName().endsWith(".groovy") ||
                            file.getName().endsWith(".txt") ||
                            file.getName().endsWith(".properties") ||
                            file.getName().endsWith(".xml")) {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        item.getChildren().add(new Label("Content:"));
                        item.getChildren().add(new TextArea(content));
                    }
                    break;

                case Job:
                    break;

                case Parameter:
                    SimpleParameterXML simple = (SimpleParameterXML) payload;

                    addTextProperty("Key:", simple::setKey, simple::getKey);
                    addTextProperty("Name:", simple::setName, simple::getName);

                    addTextAreaProperty("Create component:", simple::setCreateComponentScript,
                            simple::getCreateComponentScript);

                    addTextAreaProperty("On dependencies change:", simple::setOnDependenciesChangeScript,
                            simple::getOnDependenciesChangeScript);

                    addTextAreaProperty("Validate:", simple::setValidateScript,
                            simple::getValidateScript);
                    break;

                case Expression:
                    ExpressionXML exp= (ExpressionXML) payload;
                    item.getChildren().add(new Label("Evaluate:"));
                    item.getChildren().add(new TextArea(exp.getEvaluateScript()));
                    break;

                case Call:
                    CallXML call = (CallXML) payload;
                    // TODO
                    break;
            }
        }

        private void addTextAreaProperty(String title,
                                         Consumer<String> set,
                                         Supplier<String> get) {
            item.getChildren().add(new Label(title));
            TextArea control = new TextArea(get.get());

            control.textProperty().addListener((observable, oldValue, newValue) -> {
                set.accept(newValue);
            });
            item.getChildren().add(control);
        }

        private void addTextProperty(String title,
                                         Consumer<String> set,
                                         Supplier<String> get) {
            item.getChildren().add(new Label(title));
            TextField control = new TextField(get.get());

            control.textProperty().addListener((observable, oldValue, newValue) -> {
                set.accept(newValue);
            });
            item.getChildren().add(control);
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final class TextFieldTreeCellImpl extends TreeCell<Item> {
        private ContextMenu addDependencyMenu = new ContextMenu();
        private ContextMenu deleteMenu = new ContextMenu();

        TextFieldTreeCellImpl() {
            MenuItem addDependency = new MenuItem("Add dependency");
            this.addDependencyMenu.getItems().add(addDependency);
            addDependency.setOnAction(t -> {
                // TODO)
                TreeItem<Item> newDep = new TreeItem<>(new Item(ItemType.Dependency, "New dep", null));
                getTreeItem().getChildren().add(newDep);
            });

            MenuItem delete = new MenuItem("Delete");
            this.deleteMenu.getItems().add(delete);
            delete.setOnAction(t -> {
                // TODO)
                getTreeItem().getParent().getChildren().remove(getTreeItem());
            });
        }

        @Override
        protected void updateItem(Item item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item != null) {
                setText(item.title);
                setGraphic(getTreeItem().getGraphic());
                if (item.itemType == ItemType.Dependencies) {
                    setContextMenu(addDependencyMenu);
                } else if (item.itemType == ItemType.Dependency) {
                    setContextMenu(deleteMenu);
                }
            }
        }
    }

}