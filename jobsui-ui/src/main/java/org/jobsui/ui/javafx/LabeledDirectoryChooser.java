package org.jobsui.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.jobsui.core.ui.UIButton;
import org.jobsui.core.ui.UIValue;

import java.io.File;
import java.util.Objects;

public class LabeledDirectoryChooser extends VBox {

    private final UIValue<Node> field;

    public LabeledDirectoryChooser(JavaFXUI ui, String text) {
        Label label = new Label(text);
        label.getStyleClass().add(JobsUIFXStyles.EDIT_PROPERTY_NAME_TEXT);
        getChildren().add(label);

        HBox hBox = new HBox(10);
        getChildren().add(hBox);

        this.field = ui.createValue();
        HBox.setHgrow(field.getComponent(), Priority.ALWAYS);
        hBox.getChildren().add(field.getComponent());

        UIButton<Node> button = ui.createButton();
        button.setTitle("...");
        button.getObservable().subscribe(event -> {
            DirectoryChooser chooser = new DirectoryChooser();

            if (field.getValue() != null && !Objects.toString(field.getValue()).isEmpty()) {

                try {
                    File file = new File(Objects.toString(field.getValue()));
                    if (file.isDirectory()) {
                        chooser.setInitialDirectory(file);
                    }
                } catch (Exception e) {

                }
            }

            chooser.setTitle("Project folder");

            File file = chooser.showDialog(null);
            if (file != null) {
                field.setValue(file.getAbsolutePath());
            }

        });
        hBox.getChildren().add(button.getComponent());
    }

    public UIValue<Node> getField() {
        return field;
    }
}
