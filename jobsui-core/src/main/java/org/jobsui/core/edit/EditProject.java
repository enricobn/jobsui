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
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jobsui.core.groovy.JobParser;
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
                            itemDetail.getChildren().clear();
                            itemsTree.setRoot(null);
                            status.setText("Loading project ...");
                        });
                        try {
                            TreeItem<Item> root = loadProject(file);

                            Platform.runLater(() -> {
                                itemsTree.setRoot(root);
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

        itemsTree = new TreeView<>();
//        itemsTree.setCellFactory(param -> new TextFieldTreeCellImpl());
        itemsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    newValue.getValue().onSelect();
                } catch (Throwable e) {
                    showError("Error :", e);
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
        root.getChildren().add(splitPane);

        status = new Label();
        root.getChildren().add(status);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Edit JobsUI");
        stage.setScene(scene);
        stage.show();
    }

    private static void showError(String message, Throwable e) {
        // TODO
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, message + " " + e.getMessage());
        alert.showAndWait();
    }

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

        parametersList.stream()
                .forEach(parameter -> addParameter(parameters, itemType, parameter, jobXML));
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
                    ProjectXML project = (ProjectXML) payload;
                    addTextProperty("Name:", project::getName, project::setName);
                    break;

                case GroovyFile:
                    File file = (File) payload;
                    if (file.getName().endsWith(".groovy") ||
                            file.getName().endsWith(".txt") ||
                            file.getName().endsWith(".properties") ||
                            file.getName().endsWith(".xml")) {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        itemDetail.getChildren().add(new Label("Content:"));
                        itemDetail.getChildren().add(new TextArea(content));
                    }
                    break;

                case Job:
                    break;

                case Parameter: {
                    SimpleParameterXML parameter = (SimpleParameterXML) payload;

                    addTextProperty("Key:", parameter::getKey, parameter::setKey);
                    addTextProperty("Name:", parameter::getName, parameter::setName);

                    addTextAreaProperty("Create component:", parameter::getCreateComponentScript,
                            parameter::setCreateComponentScript);

                    addTextAreaProperty("On dependencies change:", parameter::getOnDependenciesChangeScript,
                            parameter::setOnDependenciesChangeScript);

                    addTextAreaProperty("Validate:", parameter::getValidateScript, parameter::setValidateScript);
                    break;
                }

                case Expression: {
                    ExpressionXML parameter = (ExpressionXML) payload;

                    addTextProperty("Key:", parameter::getKey, parameter::setKey);
                    addTextProperty("Name:", parameter::getName, parameter::setName);

                    addTextAreaProperty("Evaluate:", parameter::getEvaluateScript, parameter::setEvaluateScript);
                    break;
                }

                case Call: {
                    CallXML parameter = (CallXML) payload;

                    addTextProperty("Key:", parameter::getKey, parameter::setKey);
                    addTextProperty("Name:", parameter::getName, parameter::setName);

                    // TODO
                    break;
                }
            }
        }

        private void addTextAreaProperty(String title,
                                         Supplier<String> get, Consumer<String> set) {
            itemDetail.getChildren().add(new Label(title));
            TextArea control = new TextArea(get.get());

            control.textProperty().addListener((observable, oldValue, newValue) -> {
                set.accept(newValue);
            });
            itemDetail.getChildren().add(control);
        }

        private void addTextProperty(String title,
                                     Supplier<String> get, Consumer<String> set) {
            itemDetail.getChildren().add(new Label(title));
            TextField control = new TextField(get.get());

            control.textProperty().addListener((observable, oldValue, newValue) -> {
                set.accept(newValue);

                updateTreeItem(itemsTree.getSelectionModel().getSelectedItem());
            });
            itemDetail.getChildren().add(control);
        }

        @Override
        public String toString() {
            return title.get();
        }
    }

    private static void updateTreeItem(TreeItem<Item> treeItem) {
//        if (treeItem.getParent() != null) {
//            int index = treeItem.getParent().getChildren().indexOf(treeItem);
//            treeItem.getParent().getChildren().set(index, treeItem);
//        } else {
            Item value = treeItem.getValue();
            treeItem.setValue(null);
            treeItem.setValue(value);
//        }
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

        if (item.itemType == ItemType.Dependencies) {
            ParameterXML parameterXML = (ParameterXML) item.payload;
            List<String> dependencies = parameterXML.getDependencies();
            JobXML jobXML = (JobXML) treeItem.getParent().getParent().getValue().payload;
            List<String> parameters = getAllParameters(jobXML);
            parameters.removeAll(dependencies);

            if (!parameters.isEmpty()) {
                Menu addDependency = new Menu("Add dependency");
                contextMenu.getItems().add(addDependency);

                for (String dependency : parameters) {
                    ParameterXML parameter = jobXML.getParameter(dependency);
                    String name = parameter.getName();
                    MenuItem dependencyMenuItem = new MenuItem(name);
                    dependencyMenuItem.setOnAction(t -> {
                        parameterXML.addDependency(dependency);
                        TreeItem<Item> newDep = new TreeItem<>(EditProject.this.new Item(ItemType.Dependency,
                                () -> name, dependency));
                        treeItem.getChildren().add(newDep);
                    });
                    addDependency.getItems().add(dependencyMenuItem);
                }
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

    private static List<String> getAllParameters(JobXML jobXML) {
        return Stream.concat(Stream.concat(jobXML.getCallXMLs().stream(),
                jobXML.getExpressionXMLs().stream()),
                jobXML.getSimpleParameterXMLs().stream())
                .map(ParameterXML::getKey).collect(Collectors.toList());
    }

}