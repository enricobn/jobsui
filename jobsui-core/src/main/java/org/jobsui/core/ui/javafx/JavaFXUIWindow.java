package org.jobsui.core.ui.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIContainer;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UIWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/7/16.
 */
public class JavaFXUIWindow implements UIWindow<Node> {
    private final List<LabeledComponent> components = new ArrayList<>();

    private static boolean ok = false;
    private static boolean valid = false;

    @Override
    public boolean show() {
        JavaFXApplication.components.addAll(components);

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
    public <T> UIWidget<T, Node> add(String title, final UIComponent<T, Node> component) {
        components.add(new LabeledComponent(title, component));

        return new UIWidget<T, Node>() {
            @Override
            public void setVisible(boolean visible) {
                component.setVisible(visible);
            }

            @Override
            public UIComponent<T, Node> getComponent() {
                return component;
            }

            @Override
            public void setValidationMessages(List<String> messages) {
                // TODO
            }
        };
    }

    @Override
    public <T> UIWidget<T, Node> add(UIComponent<T, Node> component) {
        return add("", component);
    }

    @Override
    public void add(UIContainer<Node> container) {
        System.out.println("add container");
    }

    @Override
    public Node getComponent() {
        return JavaFXApplication.root;
    }

    public static class JavaFXApplication extends Application {
        private Scene scene;
        private static VBox root;
        private static List<LabeledComponent> components = new ArrayList<>();
        private static Button okButton;
        private static boolean valid = false;

        @Override
        public void start(Stage stage) throws Exception {
            root = new VBox();
            root.setSpacing(5);
            root.setPadding(new Insets(5, 5, 5, 5));

            for (LabeledComponent component : components) {
                VBox componentPane = new VBox();
                VBox labeled = new VBox();
                Label label = new Label(component.title);
                labeled.getChildren().add(label);
                labeled.getChildren().add(component.component.getComponent());
                componentPane.getChildren().add(labeled);

                root.getChildren().add(componentPane);
            }

            okButton = new Button("OK");
            okButton.setDisable(!valid);
            okButton.setOnAction(event -> {
                ok = true;
                Platform.exit();
            });
            root.getChildren().add(okButton);

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(event -> {
                ok = false;
                Platform.exit();
            });
            root.getChildren().add(cancelButton);

            scene = new Scene(root, 600, 800);

            stage.setTitle("JobsUI");
            stage.setScene(scene);
            stage.show();
        }

        public static void setValid(boolean valid) {
            if (okButton == null) {
                JavaFXApplication.valid = valid;
            } else {
                okButton.setDisable(!valid);
            }
        }
    }

    private static class LabeledComponent {
        final String title;
        final UIComponent<?, Node> component;

        private LabeledComponent(String title, UIComponent<?, Node> component) {
            this.title = title;
            this.component = component;
        }
    }

}

