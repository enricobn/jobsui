package org.jobsui.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.hamcrest.core.Is;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.OpenedItem;
import org.jobsui.core.job.Job;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.xml.ParameterXML;
import org.jobsui.core.xml.ProjectParser;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;
import org.jobsui.ui.javafx.JavaFXUI;
import org.jobsui.ui.javafx.StartApp;
import org.jobsui.ui.javafx.edit.EditItem;
import org.jobsui.ui.javafx.edit.ItemDetail;
import org.jobsui.ui.javafx.edit.ItemType;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 4/22/17.
 */
@RunWith(MockitoJUnitRunner.class)
@Category(UITest.class)
public class TestMain extends ApplicationTest {
    @Mock
    private JobsUIPreferences preferences;
    private OpenedItem openedItem;
    private URL projectResource;

    private void setUpPreferences() {
        projectResource = getClass().getResource("/simplejob");
        String urlString = projectResource.toExternalForm();
        openedItem = new OpenedItem(urlString, "SimpleJob");
        List<OpenedItem> openedItems = Collections.singletonList(openedItem);
        when(preferences.getLastOpenedItems()).thenReturn(openedItems);
        when(preferences.getTheme()).thenReturn(JobsUITheme.Standard);
        when(preferences.getRunWidth()).thenReturn(600d);
        when(preferences.getRunHeight()).thenReturn(600d);
        when(preferences.getEditWidth()).thenReturn(600d);
        when(preferences.getEditHeight()).thenReturn(600d);
        when(preferences.getRunDividerPosition()).thenReturn(0.4d);
        when(preferences.getEditDividerPosition()).thenReturn(0.4d);
    }

    @Override
    public void start(Stage stage) throws Exception {
        setUpPreferences();
        JavaFXUI ui = new JavaFXUI(preferences);
        StartApp.initForTest(ui);

        URL fxml = StartApp.class.getResource("Start.fxml");
        Parent page = FXMLLoader.load(fxml, null, new JavaFXBuilderFactory());
        Scene scene = new Scene(page, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void assert_that_projects_are_loaded_with_openedItems() {
        ListView<OpenedItem> projects = robotContext().getNodeFinder().lookup("#projects").query();
        assertThat(projects.getItems().iterator().next(), Is.is(openedItem));
    }

    @Test
    public void assert_that_opening_a_project_shows_a_combobox_with_all_jobs() throws Exception {
        openProject();
        ComboBox<Job> jobs = robotContext().getNodeFinder().lookup(".combo-box").query();

        ProjectParser projectParser = new ProjectParserImpl();
        ProjectXML projectXML = projectParser.parse(projectResource);

        assertThat(jobs.getItems().size(), Is.is(projectXML.getJobs().size()));
    }

    @Test
    public void check_that_opening_a_job_shows_the_run_window() throws Exception {
        ProjectParser projectParser = new ProjectParserImpl();
        ProjectXML projectXML = projectParser.parse(projectResource);
        openProject();
        ComboBox<Job> jobs = robotContext().getNodeFinder().lookup(".combo-box").query();
        assertThat(jobs.getItems().size(), Is.is(projectXML.getJobs().size()));

        Button okButton = getButtonByText("OK");
        clickOn(okButton);

        waitUntilRunButtonIsPresent();

        closeCurrentWindow();
    }

    @Test
    public void run() {
        openProject();

        Button okButton = getButtonByText("OK");
        clickOn(okButton);

        Button runButton = waitUntilRunButtonIsPresent();
        clickOn(runButton);

        closeCurrentWindow();
    }

    @Test
    public void editAndSelectAParameter() throws Exception {
        editProject();

        TreeView<EditItem> treeView = lookupWithTimeout(Objects::nonNull, 5_000);

        while (treeView.getRoot() == null) {
            sleep(100);
        }

        TreeItem<EditItem> treeViewItem = getTreeViewItem(treeView.getRoot(), ItemType.Parameter,
                it -> ((ParameterXML) it.payload).getName().equals("First"));

        Platform.runLater(() -> treeView.getSelectionModel().select(treeViewItem));

        TextField detailParameterName = lookupWithTimeout("#" + ItemDetail.ID_DETAIL_PARAMETER_NAME, 5_000);

        assertEquals("First", detailParameterName.getText());
    }


    private <T extends Node> T lookupWithTimeout(Predicate<T> predicate, long timeout) throws Exception {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + timeout) {
            try {
                return robotContext().getNodeFinder().lookup(predicate).query();
            } catch (Exception e) {
                sleep(100);
            }
        }
        throw new Exception("Timeout expired");
    }

    private <T extends Node> T lookupWithTimeout(String query, long timeout) throws Exception {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis()< time + timeout) {
            try {
                return robotContext().getNodeFinder().lookup(query).query();
            } catch (Exception e) {
                sleep(100);
            }
        }
        throw new Exception("Timeout expired");
    }

    private TreeItem<EditItem> getTreeViewItem(TreeItem<EditItem> item, ItemType itemType, Predicate<EditItem> p)
    {
        if (item != null && item.getValue().itemType == itemType && p.test(item.getValue()))
            return  item;

        if (item == null)
            return null;

        while (item.getChildren() == null) {
            sleep(100);
        }

        for (TreeItem<EditItem> child : item.getChildren()){
            TreeItem<EditItem> s = getTreeViewItem(child, itemType, p);
            if(s!=null)
                return s;

        }
        return null;
    }

    private Button waitUntilRunButtonIsPresent() {
        long time = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - time > 10_000) {
                fail("Timeout");
            }
            sleep(100);
            Button button = getButtonByText("Run");
            if (button != null) {
                // TODO I don't know why I must sleep
                sleep(500);
                return button;
            }
        }
    }

    private void openProject() {
        ListCell<OpenedItem> openedItemListCell = robotContext().getNodeFinder().lookup(
                "#openedItem_0").query();
        clickOn(openedItemListCell);
    }

    private void editProject() {
        ListCell<OpenedItem> openedItemListCell = robotContext().getNodeFinder().lookup(
                "#openedItem_0").query();
        rightClickOn(openedItemListCell);

        clickOn("#edit");
    }

    private Button getButtonByText(String text) {
        try {
            return robotContext().getNodeFinder().lookup(node -> ((Button) node).getText().equals(text)).query();
        } catch (Exception e) {
            return null;
        }
    }

}
