package org.jobsui.ui.javafx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by enrico on 10/7/16.
 */
class JavaFXUIWindow implements UIWindow<Node> {
    private final UI<Node> ui;
    private HBox buttonsPanel;
    private ListView<Bookmark> bookmarkListView;
    private VBox componentsPanel;
    private Consumer<Bookmark> onOpenBookmark;

    JavaFXUIWindow(UI<Node> ui) {
        this.ui = ui;
    }

    @Override
    public void show(Project project, Job job, Runnable callback) {
        buttonsPanel = new HBox(5);
        buttonsPanel.setPadding(new Insets(10, 5, 5, 5));

        componentsPanel = new VBox(10);
        componentsPanel.setPadding(new Insets(5, 5, 5, 5));

        VBox mainPanel = createMainPanel(buttonsPanel, componentsPanel);

        bookmarkListView = createBookmarkListView(project, job);

        VBox bookmarksPanel = createBookmarksPanel(bookmarkListView);

        SplitPane root = new SplitPane(bookmarksPanel, mainPanel);
        Platform.runLater(() -> root.setDividerPosition(0, ui.getPreferences().getRunDividerPosition()));

        callback.run();

        try {
            Stage stage = StartApp.getInstance().replaceSceneContent(root, project.getName() + " / " + job.getName());
            stage.setWidth(ui.getPreferences().getRunWidth());
            stage.setHeight(ui.getPreferences().getRunHeight());
            stage.setOnHidden(event -> {
                ui.getPreferences().setRunWidth(stage.getWidth());
                ui.getPreferences().setRunHeight(stage.getHeight());
                ui.getPreferences().setRunDividerPosition(root.getDividerPositions()[0]);
            });
            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static VBox createMainPanel(HBox buttonsPanel, VBox componentsPanel) {
        VBox mainPanel = new VBox(5);
        mainPanel.getChildren().add(buttonsPanel);
        mainPanel.getChildren().add(componentsPanel);
        return mainPanel;
    }

    private static VBox createBookmarksPanel(ListView<Bookmark> bookmarkListView) {
        VBox bookmarksPanel = new VBox(5);
        Label bookmarksLabel = new Label("Bookmarks");
        bookmarksLabel.setPadding(new Insets(5, 5, 5, 5));
        bookmarksPanel.getChildren().add(bookmarksLabel);
        bookmarksPanel.getChildren().add(bookmarkListView);
        VBox.setVgrow(bookmarkListView, Priority.ALWAYS);
        return bookmarksPanel;
    }

    private HBox createMainPanel(Project project, Job job, ListView<Bookmark> bookmarkListView) {
        HBox mainPanel = new HBox(5);
        mainPanel.getChildren().add(bookmarkListView);
        return mainPanel;
    }

    private ListView<Bookmark> createBookmarkListView(Project project, Job job) {
        ListView<Bookmark> bookmarkListView = new ListView<>();
//        bookmarkListView.setMinWidth(200);
        bookmarkListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        bookmarkListView.setCellFactory(new CellFactory(project, job));
        List<Bookmark> bookmarks = ui.getPreferences().getBookmarks(project, job);
        bookmarkListView.getItems().addAll(bookmarks);
        bookmarkListView.setBorder(Border.EMPTY);
        return bookmarkListView;
    }

    @Override
    public void showValidationMessage(String message) {
        // TODO
    }

    @Override
    public void add(UIWidget<Node> widget) {
        Node node = widget.getLayoutComponent();
        componentsPanel.getChildren().add(node);
    }

    @Override
    public void addButton(UIButton<Node> button) {
        UIWidget<Node> widget = ui.createWidget(null, button);
        buttonsPanel.getChildren().add(widget.getLayoutComponent());
    }

    @Override
    public void add(UIContainer<Node> container) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getComponent() {
        return componentsPanel;
    }

    @Override
    public void setOnOpenBookmark(Consumer<Bookmark> onOpenBookmark) {
        this.onOpenBookmark = onOpenBookmark;
    }

    @Override
    public void refreshBookmarks(Project project, Job job) {
        bookmarkListView.getItems().clear();
        bookmarkListView.getItems().addAll(ui.getPreferences().getBookmarks(project, job));
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
                    JobsUIPreferences preferences = ui.getPreferences();
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

