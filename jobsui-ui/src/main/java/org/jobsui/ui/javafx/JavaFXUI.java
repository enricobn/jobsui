package org.jobsui.ui.javafx;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jobsui.core.JobsUIMainParameters;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

/**
 * Created by enrico on 10/7/16.
 */
public class JavaFXUI implements UI<Node> {
    private final JobsUIPreferences preferences;
    private final JobsUIMainParameters parameters;

    public JavaFXUI(JobsUIPreferences preferences, JobsUIMainParameters parameters) {
        this.preferences = preferences;
        this.parameters = parameters;
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
        } else if (componentType == UIExpression.class) {
            return (COMP) new JavaFXUIExpression();
        } else if (componentType == UIValue.class) {
            return (COMP) new JavaFXUIValue(this);
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

    public static void uncaughtException(Thread t, Throwable e) {
        showErrorStatic("Error on thread " + t.getName(), e);
    }

    public static void showErrorStatic(String message, Throwable e) {
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

    public static Stage getErrorStage(String message, String errorMessage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader(JavaFXUI.class.getResource("Error.fxml"));
        try {
            Parent root = loader.load();
            ((ErrorController)loader.getController()).setMessageText(message);
            ((ErrorController)loader.getController()).setErrorText(errorMessage);

            Scene scene = new Scene(root, 600, 600);

            dialog.setScene(scene);
            dialog.setTitle("JobsUI");
            return dialog;
        } catch (IOException exc) {
            exc.printStackTrace();
            return null;
        }
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

}
