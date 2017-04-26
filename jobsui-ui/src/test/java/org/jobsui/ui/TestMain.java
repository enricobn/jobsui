package org.jobsui.ui;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.hamcrest.core.Is;
import org.jobsui.core.JobsUIMainParameters;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.OpenedItem;
import org.jobsui.core.job.Job;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.xml.ProjectParser;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;
import org.jobsui.ui.javafx.JavaFXUI;
import org.jobsui.ui.javafx.StartApp;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
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
    @Mock
    private JobsUIMainParameters parameters;
    private OpenedItem openedItem;
    private URL projectResource;

    private void setUpPreferences() throws Exception {
        projectResource = getClass().getResource("/simplejob");
        String urlString = projectResource.toExternalForm();
        openedItem = new OpenedItem(urlString, "SimpleJob");
        List<OpenedItem> openedItems = Collections.singletonList(openedItem);
        when(preferences.getLastOpenedItems()).thenReturn(openedItems);
        when(preferences.getTheme()).thenReturn(JobsUITheme.Standard);
    }

    @Override
    public void start(Stage stage) throws Exception {
        setUpPreferences();
        JavaFXUI ui = new JavaFXUI(preferences, parameters);
        StartApp.initForTest(ui);

        URL fxml = StartApp.class.getResource("Start.fxml");
        Parent page = FXMLLoader.load(fxml, null, new JavaFXBuilderFactory());
        Scene scene = new Scene(page, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void assert_that_projects_are_loaded_with_openedItems() throws Exception {
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
    public void run() throws Exception {
        openProject();

        Button okButton = getButtonByText("OK");
        clickOn(okButton);

        Button runButton = waitUntilRunButtonIsPresent();
        clickOn(runButton);

        closeCurrentWindow();
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
                return button;
            }
        }
    }

    private void openProject() {
        ListCell<OpenedItem> openedItemListCell = robotContext().getNodeFinder().lookup(
                "#openedItem_0").query();
        clickOn(openedItemListCell);
    }

    private Button getButtonByText(String text) {
        return robotContext().getNodeFinder().lookup(node -> ((Button) node).getText().equals(text)).query();
    }
}
