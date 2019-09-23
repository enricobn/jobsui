package org.jobsui.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jobsui.core.CommandLineArguments;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.*;
import org.jobsui.ui.javafx.uicomponent.*;

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
    public UIChoice<Node> createChoice() {
        return new JavaFXUIChoice(this);
    }

    @Override
    public UIList<Node> createList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIPassword<Node> createPassword() {
        return new JavaFXUIPassword(this);
    }

    @Override
    public UIValue<Node> createValue() {
        return new JavaFXUIValue(this);
    }

    @Override
    public UIButton<Node> createButton() {
        return new JavaFXUIButton(this);
    }

    @Override
    public UICheckBox<Node> createCheckBox() {
        return new JavaFXUICheckBox(this);
    }

    @Override
    public UIFileChooser<Node> createFileChooser() {
        return new JavaFXUIFileChooser(this);
    }

    @Override
    public void showError(String message, Throwable t) {
        showErrorStatic(message, t);
    }

    @Override
    public void start(CommandLineArguments arguments) {
        StartApp.main(this, arguments);
    }

    @Override
    public Optional<String> askString(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("JobsUI");
        dialog.setHeaderText(message);
        return dialog.showAndWait();
    }

    @Override
    public boolean askOKCancel(String message, boolean focusOnCancel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("JobUI");
        alert.setHeaderText(message);

        if (focusOnCancel) {
            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDefaultButton(false);

            Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setDefaultButton(true);
        }

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

    static void uncaughtException(Thread t, Throwable e) {
        showErrorStatic("Error on thread " + t.getName(), e);
    }

    private static void showErrorStatic(String message, Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("JobsUI");
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.setResizable(true);

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        Button copyButton = new Button("Copy");
        copyButton.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(exceptionText);
            clipboard.setContent(content);
        });

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
        expContent.add(copyButton, 0, 2);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
        e.printStackTrace();
    }

    private static void showMessageStatic(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JobsUI");
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.showAndWait();
    }

    static <T> Optional<T> chooseStatic(String message, List<T> choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(null, choices);
        dialog.setTitle("JobsUI");
        dialog.setHeaderText(message);
//        dialog.setContentText("Choose your letter:");

        // Traditional way to get the response value.
        return dialog.showAndWait();
    }

    private static class NodeUIWidget implements UIWidget<Node> {
        private final String title;
        private final UIComponent<Node> component;
        private final VBox nodeComponent;
        private final Label label;

        NodeUIWidget(String title, UIComponent<Node> component) {
            this.title = title;
            this.component = component;
            nodeComponent = new VBox();
            label = label(title);
            nodeComponent.getChildren().add(component.getComponent());
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
                label.getStyleClass().add(JobsUIFXStyles.FIELD_LABEL);
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

        private Label label(String text) {
            Label label = new Label(text);
            label.getStyleClass().add(JobsUIFXStyles.FIELD_LABEL);
            //if (!nodeComponent.getChildren().isEmpty()) {
                label.setPadding(new Insets(20, 0, 0, 0));
            //}
            nodeComponent.getChildren().add(label);
            return label;
        }
    }


}
