package org.jobsui.core.edit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jobsui.core.Job;
import org.jobsui.core.JobParameterDef;
import org.jobsui.core.Project;
import org.jobsui.core.groovy.JobParameterDefGroovy;
import org.jobsui.core.groovy.JobParser;

import java.io.File;
import java.util.Comparator;

/**
 * Created by enrico on 10/9/16.
 */
public class EditProject extends Application {
    private TreeView<Item> items;
    private VBox item;
    private VBox root;
    private Label status;

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
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading project: " + e.getMessage());
                            alert.showAndWait();
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
        root.getChildren().add(buttons);

        items = new TreeView<>();
        items.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.getValue().onSelect();
            }
        });

        item = new VBox(5);

        SplitPane splitPane = new SplitPane(items, item);
        root.getChildren().add(splitPane);

        status = new Label();
        root.getChildren().add(status);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Edit JobsUI");
        stage.setScene(scene);
        stage.show();
    }

    private TreeItem<Item> loadProject(File file) throws Exception {
        JobParser parser = new JobParser();
        Project project = parser.loadProject(file);
        TreeItem<Item> root = new TreeItem<>(new Item(ItemType.Project, project.getName(), project));
        project.getKeys().stream()
                .map(project::getJob)
                .sorted(Comparator.comparing(Job::getName))
                .map(this::createJobTreeItem)
                .forEach(root.getChildren()::add);
        return root;
    }

    private TreeItem<Item> createJobTreeItem(Job<?> job) {
        TreeItem<Item> result = new TreeItem<>(new Item(ItemType.Job, job.getName(), job));
        job.getParameterDefs().stream()
                .sorted(Comparator.comparing(JobParameterDef::getName))
                .map(parameterDef -> new Item(ItemType.ParameterDef, parameterDef.getName(), parameterDef))
                .map(TreeItem::new)
                .forEach(result.getChildren()::add);
        result.setExpanded(true);
        return result;
    }

    private enum ItemType {
        Project, Job, ParameterDef
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

        void onSelect() {
            item.getChildren().clear();
            switch (itemType) {
                case Project:
                    break;
                case Job:
                    break;
                case ParameterDef:
                    JobParameterDefGroovy<?> parameterDef = (JobParameterDefGroovy) payload;
                    if (!parameterDef.getDependencies().isEmpty()) {
                        item.getChildren().add(new Label("Dependencies:"));
                        parameterDef.getDependencies().stream()
                                .forEach(dep -> item.getChildren().add(new Label(dep.getName())));
                    }
                    break;
            }
        }

        @Override
        public String toString() {
            return title;
        }
    }

}
