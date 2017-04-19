package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.runner.JobValues;
import org.jobsui.core.utils.JobsUIUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.jobsui.core.TestUtils.createJob;
import static org.jobsui.core.TestUtils.createProject;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by enrico on 4/17/17.
 */
public class BookmarksStoreFSImplTest {
    private BookmarksStoreFSImpl sut;

    @Before
    public void setUp() throws Exception {
        File root = JobsUIUtils.createTempDir("bookmark", "test");
        sut = new BookmarksStoreFSImpl(root);
    }

    @Test
    public void assert_that_when_there_is_no_folder_for_project_then_bookmarks_is_empty() throws Exception {
        Project project = createProject("projectId");
        Job<?> job = createJob("jobId");
        assertThat(sut.getBookmarks(project, job).isEmpty(), is(true));
    }

    @Test
    public void assert_that_when_a_bookmark_is_saved_then_it_can_be_retrieved() throws Exception {
        Project project = createProject("projectId");
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark = new Bookmark(project, job, "bookmark", values);
        sut.saveBookmark(project, job, bookmark);

        Bookmark savedBookmark = sut.getBookmarks(project, job).get(0);

        assertThat(savedBookmark.getProjectId(), is(project.getId()));
        assertThat(savedBookmark.getJobId(), is(job.getId()));
        assertThat(savedBookmark.getName(), is(bookmark.getName()));
    }

    @Test
    public void assert_that_bookmarks_are_ordered_by_name() throws Exception {
        Project project = createProject("projectId");
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark2 = new Bookmark(project, job, "bookmark2", values);
        sut.saveBookmark(project, job, bookmark2);

        Bookmark bookmark1 = new Bookmark(project, job, "bookmark1", values);
        sut.saveBookmark(project, job, bookmark1);

        List<Bookmark> savedBookmarks = sut.getBookmarks(project, job);

        assertThat(savedBookmarks.get(0).getName(), is(bookmark1.getName()));
        assertThat(savedBookmarks.get(1).getName(), is(bookmark2.getName()));
    }

    @Test
    public void assert_that_when_you_save_a_bookmark_with_the_sam_name_then_the_old_bookmark_is_replaced_with_the_new() throws Exception {
        Project project = createProject("projectId");
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark = new Bookmark(project, job, "bookmark", values);
        sut.saveBookmark(project, job, bookmark);

        bookmark = new Bookmark(project, job, "bookmark", values);
        sut.saveBookmark(project, job, bookmark);

        List<Bookmark> bookmarks = sut.getBookmarks(project, job);

        assertThat(bookmarks.size(), is(1));

        Bookmark savedBookmark = bookmarks.get(0);

        assertThat(savedBookmark.getProjectId(), is(project.getId()));
        assertThat(savedBookmark.getJobId(), is(job.getId()));
        assertThat(savedBookmark.getName(), is(bookmark.getName()));
    }
}