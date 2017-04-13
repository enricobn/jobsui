package org.jobsui.core.ui.javafx;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jobsui.core.ui.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by enrico on 10/7/16.
 */
public class JavaFXUI implements UI<Node> {

    @Override
    public void showMessage(String message) {
        showMessageStatic(message);
    }

    @Override
    public UIWindow<Node> createWindow(String title) {
        return new JavaFXUIWindow();
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
            return (COMP) new JavaFXUIChoice();
        } else if (componentType == UICheckBox.class) {
            return (COMP) new JavaFXUICheckBox();
        } else if (componentType == UIButton.class) {
            return (COMP) new JavaFXUIButton();
        } else if (componentType == UIExpression.class) {
            return (COMP) new JavaFXUIExpression();
        } else if (componentType == UIValue.class) {
            return (COMP) new JavaFXUIValue();
        }
        throw new UnsupportedComponentException("JavaFX: cannot find component for " + componentType.getName());
    }

    @Override
    public void showError(String message, Throwable t) {
        showErrorStatic(message, t);
    }

    @Override
    public void start(String[] args) {
        StartApp.main(args);
    }

    public static void uncaughtException(Thread t, Throwable e) {
        showErrorStatic("Error on thread " + t.getName(), e);
    }

    public static void showErrorStatic(String message, Throwable e) {
        StringWriter errorMsg = new StringWriter();
        e.printStackTrace(new PrintWriter(errorMsg));

        if (Platform.isFxApplicationThread()) {
            showErrorStatic(message, errorMsg.toString());
        }
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

    private static void showErrorStatic(String message, String errorMessage) {
        Stage dialog = getErrorStage(message, errorMessage);
        if (dialog != null) {
            dialog.show();
        }
    }

    public static void showMessageStatic(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JobsUI");
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
