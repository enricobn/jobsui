package org.jobsui.core.ui.javafx;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.OpenedItem;
import org.jobsui.core.job.Job;
import org.jobsui.core.xml.ProjectFSXML;

import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by enrico on 3/29/17.
 */
public class StartController implements Initializable {
    public ListView<OpenedItem> projects;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        JobsUIPreferences preferences = JobsUIPreferencesImpl.get();
        projects.getItems().addAll(preferences.getLastOpenedItems());
        projects.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        projects.setCellFactory(new CellFactory());
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
            });
            cell.setOnMouseExited(e -> {
                projects.setCursor(Cursor.DEFAULT);
                projects.getSelectionModel().clearSelection();
            });
            cell.setOnMouseClicked(e -> {
                if (cell.getItem() != null && e.getButton() == MouseButton.PRIMARY) {
                    Task<Job<Serializable>> task = new LoadJobTask(cell.getItem().project, cell.getItem().job);
                    ProgressDialog.run(task, "Opening job", job -> StartApp.getInstance().gotoRun(job));
                }
            });

            ContextMenu menu = new ContextMenu();

            MenuItem editMenuItem = new MenuItem("Edit");
            editMenuItem.setOnAction(event -> {
                Task<ProjectFSXML> task = new LoadProjectXMLTask(cell.getItem().project);
                ProgressDialog.run(task, "Opening project", project -> StartApp.getInstance().gotoEdit(project));
            });
            menu.getItems().add(editMenuItem);
            cell.setContextMenu(menu);

            return cell;
        }
    }
}
