package org.jobsui.ui.javafx;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class LabeledDirectoryChooser extends VBox {

    private final TextField field;

    public LabeledDirectoryChooser(JavaFXUI ui, String text) {
        Label label = new Label(text);
        label.getStyleClass().add(JobsUIFXStyles.EDIT_PROPERTY_NAME_TEXT);
        getChildren().add(label);

        HBox hBox = new HBox(10);
        getChildren().add(hBox);

        this.field = ui.createTextField();
        HBox.setHgrow(field, Priority.ALWAYS);
        hBox.getChildren().add(field);

        Button button = ui.createButton();
        button.setText("...");
        button.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();

            if (field.getText() != null && !field.getText().isEmpty()) {

                try {
                    File file = new File(field.getText());
                    if (file.isDirectory()) {
                        chooser.setInitialDirectory(file);
                    }
                } catch (Exception e) {

                }
            }

            chooser.setTitle("Project folder");

            File file = chooser.showDialog(null);
            if (file != null) {
                field.setText(file.getAbsolutePath());
            }

        });
        hBox.getChildren().add(button);
    }

    public TextField getField() {
        return field;
    }
}
