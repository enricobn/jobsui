package org.jobsui.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jobsui.core.ui.UIButton;
import org.jobsui.core.ui.UIFileChooser;
import org.jobsui.core.ui.UIValue;
import rx.Observable;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class JavaFXUIFileChooser extends VBox implements UIFileChooser<Node> {

    private final UIValue<Node> field;
    private final Label label;
    private final UIButton<Node> button;
    private FileChooserType type;

    public JavaFXUIFileChooser(JavaFXUI ui) {
        label = new Label();
        label.getStyleClass().add(JobsUIFXStyles.FIELD_LABEL);
        getChildren().add(label);

        HBox hBox = new HBox(10);
        getChildren().add(hBox);

        this.field = ui.createValue();
        HBox.setHgrow(field.getComponent(), Priority.ALWAYS);
        hBox.getChildren().add(field.getComponent());

        button = ui.createButton();
        button.setTitle("...");
        button.getObservable().subscribe(event -> {
            if (type == FileChooserType.FOLDERS) {
                showFolders();
            } else {
                showFiles(type == FileChooserType.FILE_SAVE);
            }

        });
        hBox.getChildren().add(button.getComponent());
    }

    public UIValue<Node> getField() {
        return field;
    }

    @Override
    public Node getComponent() {
        return this;
    }

    @Override
    public Serializable getValue() {
        return field.getValue();
    }

    @Override
    public void notifySubscribers() {
        field.notifySubscribers();
    }

    @Override
    public void setValue(Serializable value) {
        field.setValue(value);
    }

    @Override
    public void setTitle(String label) {
        this.label.setText(label);
    }

    @Override
    public void setEnabled(boolean enable) {
        field.setEnabled(enable);
        button.setEnabled(enable);

    }

    @Override
    public Observable<Serializable> getObservable() {
        return field.getObservable();
    }

    @Override
    public void setType(FileChooserType type) {
        this.type = type;
    }

    @Override
    public void setFolder() {
        this.type = FileChooserType.FOLDERS;
    }

    @Override
    public void setFileSave() {
        this.type = FileChooserType.FILE_SAVE;
    }

    @Override
    public void setFileOpen() {
        this.type = FileChooserType.FILE_OPEN;
    }

    private void showFolders() {
        DirectoryChooser chooser = new DirectoryChooser();

        if (field.getValue() != null && !Objects.toString(field.getValue()).isEmpty()) {

            try {
                File file = new File(Objects.toString(field.getValue()));
                while (file != null && !file.isDirectory()) {
                    file = file.getParentFile();
                }
                if (file != null) {
                    chooser.setInitialDirectory(file);
                }
            } catch (Exception e) {

            }
        }

        chooser.setTitle(label.getText());

        File file = chooser.showDialog(null);
        if (file != null) {
            field.setValue(file.getAbsolutePath());
        }
    }

    private void showFiles(boolean save) {
        FileChooser chooser = new FileChooser();

        if (field.getValue() != null && !Objects.toString(field.getValue()).isEmpty()) {

            try {
                File file = new File(Objects.toString(field.getValue()));
                if (file.isDirectory()) {
                    chooser.setInitialDirectory(file);
                }
            } catch (Exception e) {

            }
        }

        chooser.setTitle(label.getText());

        File file;

        if (save) {
            file = chooser.showSaveDialog(null);
        } else {
            file = chooser.showOpenDialog(null);
        }
        if (file != null) {
            field.setValue(file.getAbsolutePath());
        }
    }
}
