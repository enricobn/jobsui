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
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/7/16.
 */
class JavaFXUIWindow implements UIWindow<Node> {
    private final UI<Node> ui;
    private HBox buttonsPanel;
    private TreeView<Bookmark> bookmarksView;
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

        bookmarksView = createBookmarksView(project, job);

        refreshBookmarks(project, job, null);

        VBox bookmarksPanel = createBookmarksPanel(bookmarksView);

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

    private static VBox createBookmarksPanel(TreeView<Bookmark> bookmarkListView) {
        VBox bookmarksPanel = new VBox(5);

        Label bookmarksLabel = new Label("Bookmarks");
        bookmarksLabel.getStyleClass().add(JobsUIFXStyles.FIELD_LABEL);
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

    private TreeView<Bookmark> createBookmarksView(Project project, Job job) {
        TreeView<Bookmark> view = new TreeView<>();
//        bookmarkListView.setMinWidth(200);
        view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        view.setCellFactory(new CellFactory(project, job));
        view.setBorder(Border.EMPTY);
        return view;
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
        List<Bookmark> bookmarks = ui.getPreferences().getBookmarks(project, job);
        TreeItem<Bookmark> root = new TreeItem<>();
        root.setExpanded(true);
        root.getChildren().setAll(bookmarks.stream().map(TreeItem::new).collect(Collectors.toList()));
        bookmarksView.setRoot(root);

        if (activeBookmark != null) {
            Optional<Bookmark> first = bookmarks.stream()
                    .filter(it -> it.getKey().equals(activeBookmark.getKey()))
                    .findFirst();

            first.ifPresent(bookmark -> bookmarksView.getSelectionModel().select(new TreeItem<>(bookmark)));
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

    private class CellFactory implements Callback<TreeView<Bookmark>, TreeCell<Bookmark>> {
        private final Project project;
        private final Job job;

        CellFactory(Project project, Job job) {
            this.project = project;
            this.job = job;
        }

        @Override
        public TreeCell<Bookmark> call(TreeView<Bookmark> lv) {
            TreeCell<Bookmark> cell = new TreeCell<Bookmark>() {
                @Override
                public void updateItem(Bookmark item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        if (item == null) {
                            setText("");
                        } else {
                            setText(item.toString());
                        }
                    }
                }
            };

            cell.setOnMouseEntered(e -> {
                bookmarksView.setCursor(Cursor.HAND);
                //bookmarkListView.getSelectionModel().select(cell.getItem());
            });

            cell.setOnMouseExited(e -> {
                bookmarksView.setCursor(Cursor.DEFAULT);
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
                    if (preferences.deleteBookmark(project, job, bookmark.getName())) {
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

