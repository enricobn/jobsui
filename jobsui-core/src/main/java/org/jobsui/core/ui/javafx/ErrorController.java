package org.jobsui.core.ui.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

/**
 * Created by enrico on 2/15/17.
 */
public class ErrorController {

    @FXML
    public TextArea error;

    public void initialize() throws Exception {
    }

    public void setErrorText(String error) {
        this.error.setText(error);
    }

    public void close(ActionEvent actionEvent) {
        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();
    }
}