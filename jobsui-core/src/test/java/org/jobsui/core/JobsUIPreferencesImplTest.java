package org.jobsui.core;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobValues;
import org.jobsui.core.ui.JobsUITheme;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;
import java.util.prefs.Preferences;

import static org.hamcrest.CoreMatchers.is;
import static org.jobsui.core.TestUtils.createJob;
import static org.jobsui.core.TestUtils.createProject;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.jobsui.core.JobsUIPreferencesImpl.*;

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

    @Before
    public void setUp() throws Exception {
        when(preferences.node(LAST_OPENED_PROJECTS_NODE)).thenReturn(lastOpenedProjects);
        when(preferences.node(OTHERS_NODE)).thenReturn(others);
        when(preferences.node(EDIT_NODE)).thenReturn(edit);
        when(preferences.node(RUN_NODE)).thenReturn(run);
    }

    @Test
    public void assert_that_default_value_for_theme_is_material() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        assertThat(sut.getTheme(), is(JobsUITheme.Material));
    }

    @Test
    public void assert_that_when_standard_theme_is_specified_then_that_theme_is_returned() throws Exception {
        when(others.get(eq(THEME), anyString())).thenReturn(JobsUITheme.Standard.name());

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        assertThat(sut.getTheme(), is(JobsUITheme.Standard));
    }

    @Test
    public void verify_that_when_the_same_theme_is_set_then_flush_is_not_called() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        sut.setTheme(sut.getTheme());

        verify(others, times(0)).flush();
    }

    @Test
    public void verify_that_when_different_theme_is_set_then_flush_is_called() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        sut.setTheme(JobsUITheme.Standard);

        verify(others, times(1)).flush();
    }

    @Test
    public void verify_that_when_last_opened_projects_size_ss_2_then_2_paths_and_2_name_are_requested() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        verify(lastOpenedProjects).getInt(eq(SIZE), anyInt());

        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_PATH_PREFIX + "0"), anyString());
        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_PATH_PREFIX + "1"), anyString());

        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_NAME_PREFIX + "0"), anyString());
        verify(lastOpenedProjects).get(eq(OPENED_PROJECT_NAME_PREFIX + "1"), anyString());

        verifyNoMoreInteractions(lastOpenedProjects);
    }

    @Test
    public void assert_that_opened_projects_are_memorized_in_insertion_order() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        when(lastOpenedProjects.get(eq(OPENED_PROJECT_PATH_PREFIX + "0"), anyString())).thenReturn("file:1");
        when(lastOpenedProjects.get(eq(OPENED_PROJECT_PATH_PREFIX + "1"), anyString())).thenReturn("file:2");

        when(lastOpenedProjects.get(eq(OPENED_PROJECT_NAME_PREFIX + "0"), anyString())).thenReturn("file1");
        when(lastOpenedProjects.get(eq(OPENED_PROJECT_NAME_PREFIX + "1"), anyString())).thenReturn("file2");

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        OpenedItem lastOpenedItem = sut.getLastOpenedItems().get(0);

        assertThat(lastOpenedItem.url, is("file:2"));
        assertThat(lastOpenedItem.name, is("file2"));

        OpenedItem firstOpenedItem = sut.getLastOpenedItems().get(1);

        assertThat(firstOpenedItem.url, is("file:1"));
        assertThat(firstOpenedItem.name, is("file1"));
    }

    @Test
    public void verify_that_when_opened_project_is_registered_then_flush_is_called() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        sut.registerOpenedProject(new URL("file:1"), "file1");

        verify(lastOpenedProjects, times(1)).flush();
    }

    @Test
    public void assert_that_when_opened_projects_are_registered_then_the_last_becomes_the_first() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));
        when(lastOpenedProjects.getInt(eq(SIZE), anyInt())).thenReturn(2);

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        sut.registerOpenedProject(new URL("file:1"), "file1");
        sut.registerOpenedProject(new URL("file:2"), "file2");

        OpenedItem openedItem = sut.getLastOpenedItems().get(0);

        assertThat(openedItem.url, is("file:2"));
        assertThat(openedItem.name, is("file2"));
    }

    @Test
    public void assert_that_when_a_project_has_no_bookmarks_then_empy_list_is_returned() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        Project project = createProject("projectId");
        Job<?> job = createJob("jobId");

        assertThat(sut.getBookmarks(project, job).isEmpty(), is(true));
    }

    @Test
    public void assert_that_when_a_job_has_no_bookmarks_then_empy_list_is_returned() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        Project project = createProject("projectId");
        Job<?> job = createJob("jobId");

        assertThat(sut.getBookmarks(project, job).isEmpty(), is(true));
    }

    @Test
    public void verify_that_when_asking_for_bookmarks_for_the_first_time_then_bookmarkstore_is_called() throws Exception {
        when(others.get(eq(THEME), anyString())).thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));

        JobsUIPreferencesImpl sut = JobsUIPreferencesImpl.get(preferences, bookmarkStore);

        Project project = createProject("projectId");
        Job<?> job = createJob("jobId");

        sut.getBookmarks(project, job);

        verify(bookmarkStore).getBookmarks(project, job);
    }

    private Bookmark createBookmark(String bookmarkName, Project project, Job<?> job) {
        JobValues values = mock(JobValues.class);
        return new Bookmark(project, job, bookmarkName, values);
    }


}