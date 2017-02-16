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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JobsUI");
        alert.setContentText(message);
        alert.showAndWait();
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
            return (COMP) new JavaFXUIChoice<>();
        } else if (componentType == UICheckBox.class) {
            return (COMP) new JavaFXUICheckBox();
        }
        throw new UnsupportedComponentException("JavaFX: cannot find component for " + componentType.getName());
    }

    public static void uncaughtException(Thread t, Throwable e) {
        showErrorStatic(e);
    }

    public static void showErrorStatic(Throwable e) {
        StringWriter errorMsg = new StringWriter();
        e.printStackTrace(new PrintWriter(errorMsg));

        if (Platform.isFxApplicationThread()) {
            showErrorStatic(errorMsg.toString());
        } else {
            e.printStackTrace();
        }
    }

    private static void showErrorStatic(String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader(JavaFXUI.class.getResource("Error.fxml"));
        try {
            Parent root = loader.load();
            ((ErrorController)loader.getController()).setErrorText(message);
            dialog.setScene(new Scene(root, 600, 600));
            dialog.setTitle("JobsUI");
            dialog.show();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

}
