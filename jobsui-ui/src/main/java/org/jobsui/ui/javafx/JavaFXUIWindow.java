package org.jobsui.ui.javafx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.*;

import java.util.List;
import java.util.Optional;
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
    private Label titleLabel;
    private Consumer<Bookmark> onDeleteBookmark;

    JavaFXUIWindow(UI<Node> ui) {
        this.ui = ui;
    }

    @Override
    public void show(Project project, Job job, Runnable callback) {
        buttonsPanel = new HBox(5);
        buttonsPanel.setPadding(new Insets(5, 5, 5, 5));

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

    private static VBox createMainPanel(Pane buttonsPanel, VBox componentsPanel) {
        VBox mainPanel = new VBox();
        VBox.setVgrow(buttonsPanel, Priority.NEVER);
        mainPanel.getChildren().add(buttonsPanel);

        VBox.setVgrow(componentsPanel, Priority.ALWAYS);
        ScrollPane scrollPane = new ScrollPane(componentsPanel);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainPanel.getChildren().add(scrollPane);

        return mainPanel;
    }

    private static VBox createBookmarksPanel(ListView<Bookmark> bookmarkListView) {
        VBox bookmarksPanel = new VBox(5);

        Label bookmarksLabel = new Label("Bookmarks");
        bookmarksLabel.getStyleClass().add(JobsUIFXStyles.FIELD_LABEL);
        bookmarksLabel.setPadding(new Insets(5, 5, 5, 5));

        bookmarksPanel.getChildren().add(bookmarksLabel);
        bookmarksPanel.getChildren().add(bookmarkListView);
        VBox.setVgrow(bookmarkListView, Priority.ALWAYS);
        return bookmarksPanel;
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
        buttonsPanel.getChildren().add(button.getComponent());
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
    public void setOnOpenBookmark(Consumer<Bookmark> consumer) {
        this.onOpenBookmark = consumer;
    }

    @Override
    public void setOnDeleteBookmark(Consumer<Bookmark> consumer) {
        this.onDeleteBookmark = consumer;
    }

    @Override
    public void refreshBookmarks(Project project, Job job, Bookmark activeBookmark) {
        bookmarkListView.getItems().clear();
        List<Bookmark> bookmarks = ui.getPreferences().getBookmarks(project, job);
        bookmarkListView.getItems().addAll(bookmarks);

        if (activeBookmark != null) {
            Optional<Bookmark> first = bookmarks.stream()
                    .filter(it -> it.getKey().equals(activeBookmark.getKey()))
                    .findFirst();

            first.ifPresent(bookmark -> bookmarkListView.getSelectionModel().select(bookmark));
        }
    }

    @Override
    public void setTitle(String title) {
        if (title == null) {
            if (titleLabel != null) {
                componentsPanel.getChildren().remove(titleLabel);
                titleLabel = null;
            }
        } else {
            if (titleLabel == null) {
                titleLabel = new Label(title);
                titleLabel.getStyleClass().add(JobsUIFXStyles.WINDOW_TITLE);
                componentsPanel.getChildren().add(0, titleLabel);
            }
            titleLabel.setText(title);
        }
    }

    @Override
    public void clear() {
        componentsPanel.getChildren().clear();
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
                //bookmarkListView.getSelectionModel().select(cell.getItem());
            });

            cell.setOnMouseExited(e -> {
                bookmarkListView.setCursor(Cursor.DEFAULT);
                //bookmarkListView.getSelectionModel().clearSelection();
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
                    if (preferences.deleteBookmark(project, job, bookmark)) {
                        refreshBookmarks(project, job, bookmark);
                        onDeleteBookmark.accept(bookmark);
                    }
                }
            });
            menu.getItems().add(editMenuItem);

            cell.setContextMenu(menu);

            return cell;
        }
    }

}

