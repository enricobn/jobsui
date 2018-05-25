package org.jobsui.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jobsui.core.ui.UIValue;

public class LabeledField extends VBox {

    private final UIValue<Node> field;

    public LabeledField(JavaFXUI ui, String text) {
        Label label = new Label(text);
        label.getStyleClass().add(JobsUIFXStyles.FIELD_LABEL);
        getChildren().add(label);

        this.field = ui.createValue();
        getChildren().add(field.getComponent());
    }

    public UIValue<Node> getField() {
        return field;
    }
}
