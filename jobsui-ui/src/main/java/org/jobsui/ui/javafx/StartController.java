package org.jobsui.ui.javafx;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.OpenedItem;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.utils.Tuple2;
import org.jobsui.core.xml.*;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by enrico on 3/29/17.
 */
public class StartController implements Initializable {
    public ListView<OpenedItem> projects;
    private JobsUIPreferences preferences;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO I don't like it
        preferences = StartApp.getInstance().getPreferences();
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
        // TODO
    }

    public void onEdit(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(StartApp.getInstance().getStage());
        if (file != null) {
            try {
                URL url = file.toURI().toURL();
                editProject(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
                        setId("openedItem_");
                    } else {
                        setText(item.toString());
                        setId("openedItem_" + getIndex());
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

                    editProject(url);
                }
            });
            menu.getItems().add(editMenuItem);

            cell.setContextMenu(menu);

            return cell;
        }
    }

    private void editProject(URL url) {
        Task<ProjectFSXML> task = new LoadProjectXMLTask(new File(url.getPath()));
        ProgressDialog.run(task, "Opening project", project -> {
            StartApp.getInstance().gotoEdit(project);
            preferences.registerOpenedProject(url, project.getName());
            projects.getItems().clear();
            projects.getItems().addAll(preferences.getLastOpenedItems());
        });

    }

    private SimpleProjectXML openProject(URL url) throws Exception {
        ProjectParserImpl projectParser = new ProjectParserImpl();
        SimpleProjectXML simpleProjectXML;
        try {
            simpleProjectXML = projectParser.parseSimple(url);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }

        if (simpleProjectXML.getJobs().size() == 1) {
            String jobFile = simpleProjectXML.getJobs().iterator().next();

            openJob(url, simpleProjectXML.getJobId(jobFile), simpleProjectXML);
        } else {
            ProjectXML projectXML = projectParser.parse(url);
            List<JobWrapper> jobWrappers = simpleProjectXML.getJobs().stream()
                    .map(job -> new JobWrapper(projectXML.getJobXML(job)))
                    .collect(Collectors.toList());

            Optional<JobWrapper> jobWrapperO = JavaFXUI.chooseStatic("Choose job", jobWrappers);

            jobWrapperO.ifPresent(jobWrapper -> {
                try {
                    openJob(url, jobWrapper.getJobId(), simpleProjectXML);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return simpleProjectXML;
    }

    private void openJob(URL url, String jobId, SimpleProjectXML simpleProjectXML) {
        Task<Tuple2<Project,Job<Serializable>>> task = new LoadJobTask(url, jobId);
        ProgressDialog.run(task, "Opening job", tuple -> {
                StartApp.getInstance().gotoRun(tuple.first, tuple.second);
            preferences.registerOpenedProject(url, simpleProjectXML.getName());
            projects.getItems().clear();
            projects.getItems().addAll(preferences.getLastOpenedItems());
        });
    }

    private static class JobWrapper {
        private final JobXML jobXML;

        private JobWrapper(JobXML jobXML) {
            Objects.requireNonNull(jobXML);
            this.jobXML = jobXML;
        }

        public String getJobId() {
            return jobXML.getId();
        }

        @Override
        public String toString() {
            return jobXML.getName();
        }

    }
}
