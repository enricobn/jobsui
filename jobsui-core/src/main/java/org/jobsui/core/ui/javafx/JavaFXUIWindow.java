package org.jobsui.core.ui.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIContainer;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UIWindow;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/7/16.
 */
class JavaFXUIWindow implements UIWindow<Node> {
    private VBox root;
//    private static final List<NodeUIWidget> components = new ArrayList<>();

//    private static boolean ok = false;
//    private static Runnable callback;

    @Override
    public void show(Runnable callback) {
        root = new VBox(5);
        root.setPadding(new Insets(5, 5, 5, 5));

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
        root.getChildren().add(node);
        return widget;
    }

    @Override
    public UIWidget<Node> add(UIComponent<Node> component) {
        return add(null, component);
    }

    @Override
    public void add(UIContainer<Node> container) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getComponent() {
//        return JavaFXApplication.root;
        return root;
    }

    public static class JavaFXApplication extends Application {
        private Scene scene;
        private VBox root;
//        private static Button okButton;
//        private static boolean valid = false;

        @Override
        public void start(Stage stage) throws Exception {
            Thread.setDefaultUncaughtExceptionHandler(JavaFXUI::uncaughtException);

            root = new VBox(5);
            root.setPadding(new Insets(5, 5, 5, 5));

//            callback.run();

//            for (NodeUIWidget widget : components) {
//                Node node = widget.getNodeComponent();
//                node.managedProperty().bind(node.visibleProperty());
//                root.getChildren().add(node);
//            }

//            HBox okCancel = new HBox(5);
//
//            okButton = new Button("OK");
//            okButton.setDisable(!valid);
//            okButton.setOnAction(event -> {
//                ok = true;
//            });
//            okCancel.getChildren().add(okButton);
//
//            Button cancelButton = new Button("Cancel");
//            cancelButton.setOnAction(event -> {
//                ok = false;
//            });
//            okCancel.getChildren().add(cancelButton);
//
//            root.getChildren().add(okCancel);

            scene = new Scene(root, 600, 800);

            stage.setTitle("JobsUI");
            stage.setScene(scene);
            stage.show();

        }

//        static void setValid(boolean valid) {
//            if (okButton == null) {
//                JavaFXApplication.valid = valid;
//            } else {
//                okButton.setDisable(!valid);
//            }
//        }
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

}

