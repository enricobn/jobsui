package org.jobsui.core.ui.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.Job;
import org.jobsui.core.ui.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jobsui.core.ui.javafx.JobsUIFXStyles.ERROR_TEXT;

/**
 * Created by enrico on 10/7/16.
 */
class JavaFXUIWindow implements UIWindow<Node> {
    private HBox buttonsPanel;
    private ListView<Bookmark> bookmarkListView;
    private VBox componentsRoot;
    private Consumer<Bookmark> onOpenBookmark;

    @Override
    public void show(Project project, Job job, Runnable callback) {
        VBox root = new VBox(5);
        buttonsPanel = new HBox(5);
        root.getChildren().add(buttonsPanel);

        HBox mainPanel = new HBox(5);
        root.getChildren().add(mainPanel);
        bookmarkListView = new ListView<>();
        bookmarkListView.setMinWidth(200);
        mainPanel.getChildren().add(bookmarkListView);
        bookmarkListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        bookmarkListView.setCellFactory(new CellFactory(project, job));

        List<Bookmark> bookmarks = StartApp.getInstance().getPreferences().getBookmarks(project, job);
        bookmarkListView.getItems().addAll(bookmarks);
        componentsRoot = new VBox(5);
        componentsRoot.setPadding(new Insets(5, 5, 5, 5));

        mainPanel.getChildren().add(componentsRoot);

        callback.run();

        try {
            Stage stage = StartApp.getInstance().replaceSceneContent(root, project.getName() + " / " + job.getName());
            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showValidationMessage(String message) {
        // TODO
    }

    @Override
    public UIWidget<Node> add(String title, final UIComponent<Node> component) {
        NodeUIWidget widget = new NodeUIWidget(title, component);
        Node node = widget.getNodeComponent();
        node.managedProperty().bind(node.visibleProperty());
        componentsRoot.getChildren().add(node);
        return widget;
    }

    @Override
    public UIWidget<Node> add(UIComponent<Node> component) {
        return add(null, component);
    }

    @Override
    public void addButton(UIButton<Node> button) {
        NodeUIWidget widget = new NodeUIWidget(null, button);
        Node node = widget.getNodeComponent();
        node.managedProperty().bind(node.visibleProperty());
        buttonsPanel.getChildren().add(node);
    }

    @Override
    public void add(UIContainer<Node> container) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getComponent() {
        return componentsRoot;
    }

    @Override
    public void setOnOpenBookmark(Consumer<Bookmark> onOpenBookmark) {
        this.onOpenBookmark = onOpenBookmark;
    }

    @Override
    public void refreshBookmarks(Project project, Job job) {
        bookmarkListView.getItems().clear();
        JobsUIPreferences preferences = StartApp.getInstance().getPreferences();
        bookmarkListView.getItems().addAll(preferences.getBookmarks(project, job));
    }

    private static class NodeUIWidget implements UIWidget<Node> {
        private final String title;
        private final UIComponent<Node> component;
        private final HBox nodeComponent;
        private final Label label;

        NodeUIWidget(String title, UIComponent<Node> component) {
            this.title = title;
            this.component = component;
            nodeComponent = new HBox(2);
            label = new Label(title == null || title.isEmpty() ? null : title + ":");
            if (title != null && !title.isEmpty() ) {
                label.setMinWidth(100);
            }
            label.setAlignment(Pos.BOTTOM_RIGHT);
            nodeComponent.getChildren().add(label);
            nodeComponent.getChildren().add(component.getComponent());
            component.getComponent().setTranslateY(-5);
        }

        @Override
        public void setVisible(boolean visible) {
            nodeComponent.setVisible(visible);
        }

        @Override
        public UIComponent<Node> getComponent() {
            return component;
        }

        @Override
        public void setValidationMessages(List<String> messages) {
            if (messages.isEmpty()) {
                label.getStyleClass().clear();
                label.getStyleClass().add("label");
                label.setTooltip(null);
            } else {
                String text = messages.stream().collect(Collectors.joining(","));
                label.setTooltip(new Tooltip(text));
                label.getStyleClass().add(ERROR_TEXT);
            }
        }

        public String getTitle() {
            return title;
        }

        Node getNodeComponent() {
            return nodeComponent;
        }
    }

    private class CellFactory implements Callback<ListView<Bookmark>, ListCell<Bookmark>> {
        private final Project project;
        private final Job job;

        CellFactory(Project project, Job job) {
            this.project = project;
            this.job = job;
        }

        @Override
        public ListCell<Bookmark> call(ListView<Bookmark> lv) {
            ListCell<Bookmark> cell = new ListCell<Bookmark>() {
                @Override
                public void updateItem(Bookmark item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };

            cell.setOnMouseEntered(e -> {
                bookmarkListView.setCursor(Cursor.HAND);
                bookmarkListView.getSelectionModel().select(cell.getItem());
            });

            cell.setOnMouseExited(e -> {
                bookmarkListView.setCursor(Cursor.DEFAULT);
                bookmarkListView.getSelectionModel().clearSelection();
            });

            cell.setOnMouseClicked(e -> {
                Bookmark item = cell.getItem();
                if (item != null && e.getButton() == MouseButton.PRIMARY) {
                    try {
                        if (onOpenBookmark != null) {
                            onOpenBookmark.accept(item);
                        }
                    } catch (Exception e1) {
                        // TODO message
                        throw new RuntimeException(e1);
                    }
                }
            });

            ContextMenu menu = new ContextMenu();

            MenuItem editMenuItem = new MenuItem("Delete");
            editMenuItem.setOnAction(event -> {
                // TODO I don't want to add the edit menu for "not file"
                Bookmark bookmark = cell.getItem();
                if (bookmark != null) {
                    JobsUIPreferences preferences = StartApp.getInstance().getPreferences();
                    if (preferences.deleteBookmark(project, job, bookmark.getName())) {
                        refreshBookmarks(project, job);
                    }
                }
            });
            menu.getItems().add(editMenuItem);

            cell.setContextMenu(menu);

            return cell;
        }
    }

}

