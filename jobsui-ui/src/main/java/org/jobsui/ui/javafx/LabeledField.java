package org.jobsui.ui.javafx;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LabeledField extends VBox {

    private final TextField field;

    public LabeledField(JavaFXUI ui, String text) {

        Label label = new Label(text);
        label.getStyleClass().add(JobsUIFXStyles.EDIT_PROPERTY_NAME_TEXT);
        getChildren().add(label);

        this.field = ui.createTextField();
        getChildren().add(field);
    }

    public TextField getField() {
        return field;
    }
}
