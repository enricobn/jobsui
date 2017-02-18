package org.jobsui.core.ui.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIContainer;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UIWindow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/7/16.
 */
class JavaFXUIWindow implements UIWindow<Node> {
    private static final List<NodeUIWidget> components = new ArrayList<>();

    private static boolean ok = false;
    private static Runnable callback;

    @Override
    public boolean show(Runnable callback) {
        JavaFXUIWindow.callback = callback;

        Application.launch(JavaFXApplication.class);

        return ok;
    }

    @Override
    public void setValid(boolean valid) {
        JavaFXApplication.setValid(valid);
    }

    @Override
    public void showValidationMessage(String message) {

    }

    @Override
    public <T extends Serializable> UIWidget<T, Node> add(String title, final UIComponent<T, Node> component) {
        NodeUIWidget<T> widget = new NodeUIWidget<>(title, component);
        components.add(widget);
        return widget;
    }

    @Override
    public <T extends Serializable> UIWidget<T, Node> add(UIComponent<T, Node> component) {
        return add(null, component);
    }

    @Override
    public void add(UIContainer<Node> container) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getComponent() {
        return JavaFXApplication.root;
    }

    public static class JavaFXApplication extends Application {
        private Scene scene;
        private static VBox root;
        private static Button okButton;
        private static boolean valid = false;

        @Override
        public void start(Stage stage) throws Exception {
            Thread.setDefaultUncaughtExceptionHandler(JavaFXUI::uncaughtException);

            root = new VBox(5);
            root.setPadding(new Insets(5, 5, 5, 5));

            callback.run();

            for (NodeUIWidget widget : components) {
                Node node = widget.getNodeComponent();
                node.managedProperty().bind(node.visibleProperty());
                root.getChildren().add(node);
            }

            HBox okCancel = new HBox(5);

            okButton = new Button("OK");
            okButton.setDisable(!valid);
            okButton.setOnAction(event -> {
                ok = true;
                Platform.exit();
            });
            okCancel.getChildren().add(okButton);

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(event -> {
                ok = false;
                Platform.exit();
            });
            okCancel.getChildren().add(cancelButton);

            root.getChildren().add(okCancel);

            scene = new Scene(root, 600, 800);

            stage.setTitle("JobsUI");
            stage.setScene(scene);
            stage.show();

        }

        static void setValid(boolean valid) {
            if (okButton == null) {
                JavaFXApplication.valid = valid;
            } else {
                okButton.setDisable(!valid);
            }
        }
    }

    private static class NodeUIWidget<T extends Serializable> implements UIWidget<T, Node> {
        private final String title;
        private final UIComponent<T, Node> component;
        private final VBox nodeComponent;
        private final Label messagesLabel;

        NodeUIWidget(String title, UIComponent<T, Node> component) {
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
        public UIComponent<T, Node> getComponent() {
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

