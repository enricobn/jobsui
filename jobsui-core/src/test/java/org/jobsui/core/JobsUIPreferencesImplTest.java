package org.jobsui.core;

import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.history.RunHistoryStore;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.JobsUITheme;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import static org.hamcrest.CoreMatchers.is;
import static org.jobsui.core.JobsUIPreferencesImpl.*;
import static org.jobsui.core.TestUtils.createJob;
import static org.jobsui.core.TestUtils.createProject;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by enrico on 4/15/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JobsUIPreferencesImplTest {
    @Mock
    private Preferences preferences;
    @Mock
    private Preferences lastOpenedProjects;
    @Mock
    private Preferences others;
    @Mock
    private BookmarksStore bookmarkStore;
    @Mock
    private java.util.prefs.Preferences edit;
    @Mock
    private java.util.prefs.Preferences run;
    @Mock
    private RunHistoryStore runHistoryStore;

    @Before
    public void setUp() {
        when(preferences.node(LAST_OPENED_PROJECTS_NODE)).thenReturn(lastOpenedProjects);
        when(preferences.node(OTHERS_NODE)).thenReturn(others);
        when(preferences.node(EDIT_NODE)).thenReturn(edit);
        when(preferences.node(RUN_NODE)).thenReturn(run);

        when(run.get(eq(PROJECTS_HOME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));
    }

    @Test
    public void assert_that_default_value_for_theme_is_material() {
        JobsUIPreferencesImpl sut = getSut();

        assertThat(sut.getTheme(), is(JobsUITheme.Material));
    }

    @Test
    public void assert_that_when_standard_theme_is_specified_then_that_theme_is_returned() {
        when(others.get(eq(THEME), anyString())).thenReturn(JobsUITheme.Standard.name());

        JobsUIPreferencesImpl sut = getSut();

        assertThat(sut.getTheme(), is(JobsUITheme.Standard));
    }

    @Test
    public void verify_that_when_the_same_theme_is_set_then_flush_is_not_called() throws Exception {
        JobsUIPreferencesImpl sut = getSut();

        sut.setTheme(sut.getTheme());

        verify(others, times(0)).flush();
    }

    @Test
    public void verify_that_when_different_theme_is_set_then_flush_is_called() throws Exception {
        JobsUIPreferencesImpl sut = getSut();

        sut.setTheme(JobsUITheme.Standard);

        verify(others, times(1)).flush();
    }

    @Test
    public void verify_that_when_last_opened_projects_size_is_2_then_2_paths_and_2_name_are_requested() throws Exception {
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        getSut();

        verify(lastOpenedProjects).getInt(eq(SIZE), anyInt());

        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_PATH_PREFIX + "0"), anyString());
        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_PATH_PREFIX + "1"), anyString());

        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_NAME_PREFIX + "0"), anyString());
        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_NAME_PREFIX + "1"), anyString());

        verifyNoMoreInteractions(lastOpenedProjects);
    }

    @Test
    public void assert_that_opened_projects_are_memorized_in_insertion_order() {
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        when(lastOpenedProjects.get(eq(OPENED_PROJECT_PATH_PREFIX + "0"), anyString())).thenReturn("file:1");
        when(lastOpenedProjects.get(eq(OPENED_PROJECT_PATH_PREFIX + "1"), anyString())).thenReturn("file:2");

        when(lastOpenedProjects.get(eq(OPENED_PROJECT_NAME_PREFIX + "0"), anyString())).thenReturn("file1");
        when(lastOpenedProjects.get(eq(OPENED_PROJECT_NAME_PREFIX + "1"), anyString())).thenReturn("file2");

        JobsUIPreferencesImpl sut = getSut();

        OpenedItem lastOpenedItem = sut.getLastOpenedItems().get(0);

        assertThat(lastOpenedItem.getUrl(), is("file:2"));
        assertThat(lastOpenedItem.getName(), is("file2"));

        OpenedItem firstOpenedItem = sut.getLastOpenedItems().get(1);

        assertThat(firstOpenedItem.getUrl(), is("file:1"));
        assertThat(firstOpenedItem.getName(), is("file1"));
    }

    @Test
    public void verify_that_when_opened_project_is_registered_then_flush_is_called() throws Exception {
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        JobsUIPreferencesImpl sut = getSut();

        sut.registerOpenedProject(new URL("file:1"), "file1");

        verify(lastOpenedProjects, times(1)).flush();
    }

    @Test
    public void assert_that_when_opened_projects_are_registered_then_the_last_becomes_the_first() throws Exception {
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        JobsUIPreferencesImpl sut = getSut();

        sut.registerOpenedProject(new URL("file:1"), "file1");
        sut.registerOpenedProject(new URL("file:2"), "file2");

        OpenedItem openedItem = sut.getLastOpenedItems().get(0);

        assertThat(openedItem.getUrl(), is("file:2"));
        assertThat(openedItem.getName(), is("file2"));
    }

    @Test
    public void assert_that_when_a_project_has_no_bookmarks_then_empy_list_is_returned() throws Exception {
        JobsUIPreferencesImpl sut = getSut();

        Project project = createProject("test:projectId");
        Job<?> job = createJob("jobId");

        assertThat(sut.getBookmarks(project, job).isEmpty(), is(true));
    }

    @Test
    public void assert_that_when_a_job_has_no_bookmarks_then_empy_list_is_returned() throws Exception {
        JobsUIPreferencesImpl sut = getSut();

        Project project = createProject("test:projectId");
        Job<?> job = createJob("jobId");

        assertThat(sut.getBookmarks(project, job).isEmpty(), is(true));
    }

    @Test
    public void verify_that_when_asking_for_bookmarks_for_the_first_time_then_bookmarkstore_is_called() throws Exception {
        JobsUIPreferencesImpl sut = getSut();

        Project project = createProject("test:projectId");
        Job<?> job = createJob("jobId");

        sut.getBookmarks(project, job);

        verify(bookmarkStore).getBookmarks(project, job);
    }

    @Test
    public void assert_that_given_the_default_configuration_when_asking_for_projects_home_then_the_jobsui_folder_in_user_home_is_returned() {
        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore, runHistoryStore);

        String projectsHome = Paths.get(
                System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")),
                "jobsui")
                .toFile()
                .getAbsolutePath();
        assertThat(sut.getProjectsHome(), is(new File(projectsHome)));
    }

    @Test
    public void verify_that_project_home_is_saved() {
        JobsUIPreferencesImpl sut = getSut();

        sut.setProjectsHome(new File("afile"));

        verify(run).put(PROJECTS_HOME, new File("afile").getAbsolutePath());
    }

    private JobsUIPreferencesImpl getSut() {
        return JobsUIPreferencesImpl.get(preferences, bookmarkStore, runHistoryStore);
    }
}