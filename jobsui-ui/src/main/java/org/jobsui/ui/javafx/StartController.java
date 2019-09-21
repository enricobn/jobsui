package org.jobsui.ui.javafx;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.jobsui.core.xml.JobXML;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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
        StartApp.getInstance().gotoNew();
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
                        url = new URL(cell.getItem().getUrl());
                        openProject(url);
                    } catch (Exception e1) {
                        // TODO message
                        throw new RuntimeException(e1);
                    }
                }
            });

            ContextMenu menu = new ContextMenu();

            MenuItem edit = addMenu("Edit", menu, event -> {
                // TODO I don't want to add the edit menu for "not file"
                if (cell.getItem() != null && cell.getItem().getUrl().startsWith("file:/")) {
                    URL url;
                    try {
                        url = new URL(cell.getItem().getUrl());
                    } catch (MalformedURLException e1) {
                        // TODO message
                        throw new RuntimeException(e1);
                    }

                    editProject(url);
                }
            });

            edit.setId("edit");

            addMenu("Delete", menu, event -> {
                preferences.removeLastOpenedItem(cell.getItem());

                projects.getItems().clear();

                projects.getItems().addAll(preferences.getLastOpenedItems());
            });

            cell.setContextMenu(menu);

            return cell;
        }

        private MenuItem addMenu(String text, ContextMenu menu, EventHandler<ActionEvent> actionEventEventHandler) {
            MenuItem menuItem = new MenuItem(text);

            menuItem.setOnAction(actionEventEventHandler);

            menu.getItems().add(menuItem);

            return menuItem;
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

    private ProjectXML openProject(URL url) {
        ProjectParserImpl projectParser = new ProjectParserImpl();
        ProjectXML simpleProjectXML;
        try {
            simpleProjectXML = projectParser.parse(url);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }

        if (simpleProjectXML.getJobs().size() == 1) {
            JobXML job = simpleProjectXML.getJobs().iterator().next();

            openJob(url, job.getId(), simpleProjectXML);
        } else {
            List<JobWrapper> jobWrappers = simpleProjectXML.getJobs().stream()
                    .sorted(Comparator.comparing(JobXML::getName))
                    .map(JobWrapper::new)
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

    private void openJob(URL url, String jobId, ProjectXML simpleProjectXML) {
        Task<Tuple2<Project,Job<Serializable>>> task = new LoadJobTask(StartApp.getUi(), url, jobId, preferences.getBookmarksStore());
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
