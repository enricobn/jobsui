package org.jobsui.core.ui.javafx;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.OpenedItem;
import org.jobsui.core.job.Job;
import org.jobsui.core.xml.JobXML;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by enrico on 3/29/17.
 */
public class StartController implements Initializable {
    public ListView<OpenedItem> projects;
    private JobsUIPreferences preferences;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        preferences = JobsUIPreferencesImpl.get();
        projects.getItems().addAll(preferences.getLastOpenedItems());
        projects.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        projects.setCellFactory(new CellFactory());
    }

    public void onOpen(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(StartApp.getInstance().getStage());
        if (file != null) {
            try {
                URL url = file.toURI().toURL();
                openProject(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
                    URL url;
                    try {
                        url = new URL(cell.getItem().url);
                        openProject(url);
                    } catch (Exception e1) {
                        // TODO message
                        throw new RuntimeException(e1);
                    }
                }
            });

            ContextMenu menu = new ContextMenu();

            MenuItem editMenuItem = new MenuItem("Edit");
            editMenuItem.setOnAction(event -> {
                // TODO I don't want to add the edit menu for "not file"
                if (cell.getItem() != null && cell.getItem().url.startsWith("file:/")) {
                    URL url;
                    try {
                        url = new URL(cell.getItem().url);
                    } catch (MalformedURLException e1) {
                        // TODO message
                        throw new RuntimeException(e1);
                    }

                    Task<ProjectFSXML> task = new LoadProjectXMLTask(new File(url.getPath()));
                    ProgressDialog.run(task, "Opening project", project -> StartApp.getInstance().gotoEdit(project));
                }
            });
            menu.getItems().add(editMenuItem);

            cell.setContextMenu(menu);

            return cell;
        }
    }

    private ProjectXML openProject(URL url) throws Exception {
        ProjectParserImpl projectParser = new ProjectParserImpl();
        ProjectXML projectXML;
        try {
            projectXML = projectParser.parseSimple(url);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }

        if (projectXML.getJobs().size() == 1) {
            String jobFile = projectXML.getJobs().iterator().next();
            Task<Job<Serializable>> task = new LoadJobTask(url, projectXML.getJobId(jobFile));
            ProgressDialog.run(task, "Opening job", job -> StartApp.getInstance().gotoRun(job));
            preferences.registerOpenedProject(url, projectXML.getName());
            projects.getItems().clear();
            projects.getItems().addAll(preferences.getLastOpenedItems());
        } else {
            JavaFXUI.showMessageStatic("TODO: only one job is supported!");
        }
        return projectXML;
    }
}
