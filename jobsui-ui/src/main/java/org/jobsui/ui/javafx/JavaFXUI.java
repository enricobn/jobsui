package org.jobsui.ui.javafx;

import com.jfoenix.controls.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import static org.jobsui.ui.javafx.JobsUIFXStyles.VALIDATION_ERROR_TEXT;

/**
 * Created by enrico on 10/7/16.
 */
public class JavaFXUI implements UI<Node> {
    private final JobsUIPreferences preferences;

    public JavaFXUI(JobsUIPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void showMessage(String message) {
        showMessageStatic(message);
    }

    @Override
    public UIWindow<Node> createWindow(String title) {
        return new JavaFXUIWindow(this);
    }

    @Override
    public void log(String message) {

    }

    @Override
    public void log(String message, Throwable th) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public <COMP extends UIComponent> COMP create(Class<COMP> componentType) throws UnsupportedComponentException {
        if (componentType == UIChoice.class) {
            return (COMP) new JavaFXUIChoice(this);
        } else if (componentType == UICheckBox.class) {
            return (COMP) new JavaFXUICheckBox(this);
        } else if (componentType == UIButton.class) {
            return (COMP) new JavaFXUIButton(this);
        } else if (componentType == UIValue.class) {
            return (COMP) new JavaFXUIValue(this);
        } else if (componentType == UIPassword.class) {
            return (COMP) new JavaFXUIPassword(this);
        }
        throw new UnsupportedComponentException("JavaFX: cannot find component for " + componentType.getName());
    }

    @Override
    public void showError(String message, Throwable t) {
        showErrorStatic(message, t);
    }

    @Override
    public void start() {
        StartApp.main(this);
    }

    @Override
    public Optional<String> askString(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("JobsUI");
        dialog.setHeaderText(message);
        return dialog.showAndWait();
    }

    @Override
    public boolean askOKCancel(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("JobUI");
        alert.setHeaderText(message);
//        alert.setContentText("Are you ok with this?");

        Optional<ButtonType> resultO = alert.showAndWait();
        ButtonType result = resultO.orElse(ButtonType.CANCEL);
        return result == ButtonType.OK;
    }

    @Override
    public JobsUIPreferences getPreferences() {
        return preferences;
    }


    @Override
    public UIWidget<Node> createWidget(String title, final UIComponent<Node> component) {
        NodeUIWidget widget = new NodeUIWidget(title, component);
        Node node = widget.getLayoutComponent();
        node.managedProperty().bind(node.visibleProperty());
        return widget;
    }

    public static void uncaughtException(Thread t, Throwable e) {
        showErrorStatic("Error on thread " + t.getName(), e);
    }

    private static void showErrorStatic(String message, Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("JobsUI");
        alert.setHeaderText("Error");
        alert.setContentText(message);

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
        e.printStackTrace();
    }

    public static void showMessageStatic(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JobsUI");
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static <T> Optional<T> chooseStatic(String message, List<T> choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(null, choices);
        dialog.setTitle("JobsUI");
        dialog.setHeaderText(message);
//        dialog.setContentText("Choose your letter:");

        // Traditional way to get the response value.
        return dialog.showAndWait();
    }

    public Button createButton() {
        Button result;
        if (getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXButton();
            result.getStyleClass().add("button-raised");
        } else {
            result = new Button();
        }
        return result;
    }

    public <T> ComboBox<T> createComboBox() {
        ComboBox<T> result;
        if (getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXComboBox<>();
        } else {
            result = new ComboBox<>();
        }
        return result;
    }

    public CheckBox createCheckBox() {
        CheckBox result;
        if (getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXCheckBox();
        } else {
            result = new CheckBox();
        }
        return result;
    }

    public TextField createTextField() {
        TextField result;
        if (getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXTextField();
        } else {
            result = new TextField();
        }
        return result;
    }

    public PasswordField createPasswordField() {
        PasswordField result;
        if (getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXPasswordField();
        } else {
            result = new PasswordField();
        }
        return result;
    }

    private static class NodeUIWidget implements UIWidget<Node> {
        private final String title;
        private final UIComponent<Node> component;
        private final GridPane nodeComponent;
        private final Label label;

        NodeUIWidget(String title, UIComponent<Node> component) {
            this.title = title;
            this.component = component;
            nodeComponent = new GridPane();
            label = new Label(title == null || title.isEmpty() ? null : title + ":");
            if (title != null && !title.isEmpty() ) {
                label.setMinWidth(100);
            }
            label.setAlignment(Pos.BOTTOM_RIGHT);
            nodeComponent.add(label, 0, 0);
            GridPane.setValignment(label, VPos.BOTTOM);
            GridPane.setMargin(label, new Insets(0, 5, 0, 0));
            nodeComponent.add(component.getComponent(), 1, 0);
        }

        @Override
        public void setVisible(boolean visible) {
            nodeComponent.setVisible(visible);
        }

        @Override
        public void setDisable(boolean value) {
            nodeComponent.setDisable(value);
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
                String text = String.join(",", messages);
                label.setTooltip(new Tooltip(text));
                label.getStyleClass().add(VALIDATION_ERROR_TEXT);
            }
        }

        @Override
        public boolean isEnabled() {
            return !nodeComponent.isDisabled();
        }

        public String getTitle() {
            return title;
        }

        @Override
        public Node getLayoutComponent() {
            return nodeComponent;
        }
    }


}
