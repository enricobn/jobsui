package org.jobsui.core.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.Project;
import org.jobsui.core.job.Job;
import org.jobsui.core.ui.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/7/16.
 */
class JavaFXUIWindow implements UIWindow<Node> {
    private VBox root;
    private HBox buttonsPanel;
    private HBox mainPanel;
    private ListView<Bookmark> bookmarkListView;
    private VBox componentsRoot;
    private Consumer<Bookmark> onOpenBookmark;
//    private static final List<NodeUIWidget> components = new ArrayList<>();

//    private static boolean ok = false;
//    private static Runnable callback;

    @Override
    public void show(Project project, Job job, Runnable callback) {
        root = new VBox(5);
        buttonsPanel = new HBox(5);
        root.getChildren().add(buttonsPanel);

        mainPanel = new HBox(5);
        root.getChildren().add(mainPanel);
        bookmarkListView = new ListView<>();
        bookmarkListView.setMinWidth(200);
        mainPanel.getChildren().add(bookmarkListView);
        bookmarkListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        bookmarkListView.setCellFactory(new CellFactory());

        List<Bookmark> bookmarks = StartApp.getInstance().getPreferences().getBookmarks(project, job);
        bookmarkListView.getItems().addAll(bookmarks);
        componentsRoot = new VBox(5);
        componentsRoot.setPadding(new Insets(5, 5, 5, 5));

        mainPanel.getChildren().add(componentsRoot);

        callback.run();

        try {
            Stage stage = StartApp.getInstance().replaceSceneContent(root);
            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        JavaFXUIWindow.callback = callback;

//        Application.launch(JavaFXUIWindow.JavaFXApplication.class);
    }

//    @Override
//    public void setValid(boolean valid) {
//        JavaFXApplication.setValid(valid);
//    }

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
//        return JavaFXApplication.root;
        return componentsRoot;
    }

//    public static class JavaFXApplication extends Application {
//        private Scene scene;
//        private VBox root;
////        private static Button okButton;
////        private static boolean valid = false;
//
//        @Override
//        public void start(Stage stage) throws Exception {
//            Thread.setDefaultUncaughtExceptionHandler(JavaFXUI::uncaughtException);
//
//            root = new VBox(5);
//            root.setPadding(new Insets(5, 5, 5, 5));
//
////            callback.run();
//
////            for (NodeUIWidget widget : components) {
////                Node node = widget.getNodeComponent();
////                node.managedProperty().bind(node.visibleProperty());
////                root.getChildren().add(node);
////            }
//
////            HBox okCancel = new HBox(5);
////
////            okButton = new Button("OK");
////            okButton.setDisable(!valid);
////            okButton.setOnAction(event -> {
////                ok = true;
////            });
////            okCancel.getChildren().add(okButton);
////
////            Button cancelButton = new Button("Cancel");
////            cancelButton.setOnAction(event -> {
////                ok = false;
////            });
////            okCancel.getChildren().add(cancelButton);
////
////            root.getChildren().add(okCancel);
//
//            scene = new Scene(root, 600, 800);
//
//            stage.setTitle("JobsUI");
//            stage.setScene(scene);
//            stage.show();
//        }
//
////        static void setValid(boolean valid) {
////            if (okButton == null) {
////                JavaFXApplication.valid = valid;
////            } else {
////                okButton.setDisable(!valid);
////            }
////        }
//    }


    @Override
    public void setOnOpenBookmark(Consumer<Bookmark> onOpenBookmark) {
        this.onOpenBookmark = onOpenBookmark;
    }

    private static class NodeUIWidget implements UIWidget<Node> {
        private final String title;
        private final UIComponent<Node> component;
        private final VBox nodeComponent;
        private final Label messagesLabel;

        NodeUIWidget(String title, UIComponent<Node> component) {
            this.title = title;
            this.component = component;
            nodeComponent = new VBox(2);
            Label label = new Label(title);
            nodeComponent.getChildren().add(label);
            nodeComponent.getChildren().add(component.getComponent());
            messagesLabel = new Label();
            nodeComponent.getChildren().add(messagesLabel);
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
            String text = messages.stream().collect(Collectors.joining(","));
            messagesLabel.setText(text);
        }

        public String getTitle() {
            return title;
        }

        Node getNodeComponent() {
            return nodeComponent;
        }
    }

    private class CellFactory implements Callback<ListView<Bookmark>, ListCell<Bookmark>> {
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

//            ContextMenu menu = new ContextMenu();
//
//            MenuItem editMenuItem = new MenuItem("Edit");
//            editMenuItem.setOnAction(event -> {
//                // TODO I don't want to add the edit menu for "not file"
//                if (cell.getItem() != null && cell.getItem().url.startsWith("file:/")) {
//                    URL url;
//                    try {
//                        url = new URL(cell.getItem().url);
//                    } catch (MalformedURLException e1) {
//                        // TODO message
//                        throw new RuntimeException(e1);
//                    }
//
//                    Task<ProjectFSXML> task = new LoadProjectXMLTask(new File(url.getPath()));
//                    ProgressDialog.run(task, "Opening project", project -> StartApp.getInstance().gotoEdit(project));
//                }
//            });
//            menu.getItems().add(editMenuItem);
//
//            cell.setContextMenu(menu);

            return cell;
        }
    }


}

