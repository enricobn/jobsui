package org.jobsui.core.ui.javafx;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.OpenedItem;
import org.jobsui.core.job.Job;

import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by enrico on 3/29/17.
 */
public class StartController implements Initializable {
    public ListView<OpenedItem> projects;
    private OpenedItem selectedproject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        JobsUIPreferences preferences = JobsUIPreferencesImpl.get();
        projects.getItems().addAll(preferences.getLastOpenedItems());
        projects.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        projects.setCellFactory(new CellFactory());
    }

    public void projectOnMouseClicked(MouseEvent mouseEvent) {
        if (selectedproject != null) {
            System.out.println("Selected " + selectedproject);

            Task<Job<Serializable>> task = new LoadJobTask(selectedproject.project, selectedproject.job);

            ProgressDialog.run(task, "Opening job", job -> StartApp.getInstance().gotoRun(job));
        }
    }

    public void onOpen(ActionEvent actionEvent) {

    }

    public void onNew(ActionEvent actionEvent) {

    }

    private class CellFactory implements Callback<ListView<OpenedItem>, ListCell<OpenedItem>> {
        @Override
        public ListCell<OpenedItem> call(ListView<OpenedItem> lv) {
            ListCell<OpenedItem> cell = new ListCell<OpenedItem>() {
                @Override
                public void updateItem(OpenedItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };
            cell.setOnMouseEntered(e -> {
                projects.setCursor(Cursor.HAND);
                projects.getSelectionModel().select(cell.getItem());
                selectedproject = cell.getItem();
            });
            cell.setOnMouseExited(e -> {
                projects.setCursor(Cursor.DEFAULT);
                projects.getSelectionModel().clearSelection();
                selectedproject = null;
            });

            return cell;
        }
    }
}
