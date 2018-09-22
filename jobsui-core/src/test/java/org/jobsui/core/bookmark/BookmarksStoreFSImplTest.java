package org.jobsui.core.bookmark;

import org.jobsui.core.TestUtils;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobValues;
import org.jobsui.core.utils.JobsUIUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.jobsui.core.TestUtils.createJob;
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
        Project project = createProject();
        Job<?> job = createJob("jobId");
        assertThat(sut.getBookmarks(project, job).isEmpty(), is(true));
    }

    @Test
    public void assert_that_when_a_bookmark_is_saved_then_it_can_be_retrieved() throws Exception {
        Project project = createProject();
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark = createBookmark(project, job, values);
        sut.saveBookmark(project, job, bookmark);

        Bookmark savedBookmark = getBookmarks(project, job).get(0);

        assertThat(savedBookmark.getProjectId(), is(project.getId().toCompatibleProjectId()));
        assertThat(savedBookmark.getJobId(), is(job.getCompatibleJobId()));
        assertThat(savedBookmark.getName(), is(bookmark.getName()));
    }

    @Test
    public void assert_that_bookmarks_are_ordered_by_name() throws Exception {
        Project project = createProject();
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark2 = createBookmark(project, job, values, "bookmark2");
        sut.saveBookmark(project, job, bookmark2);

        Bookmark bookmark1 = createBookmark(project, job, values, "bookmark1");
        sut.saveBookmark(project, job, bookmark1);

        List<Bookmark> savedBookmarks = getBookmarks(project, job);

        assertThat(savedBookmarks.get(0).getName(), is(bookmark1.getName()));
        assertThat(savedBookmarks.get(1).getName(), is(bookmark2.getName()));
    }

    @Test
    public void assert_that_when_you_save_a_bookmark_with_the_sam_name_then_the_old_bookmark_is_replaced_with_the_new() throws Exception {
        Project project = createProject();
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark = createBookmark(project, job, values);
        sut.saveBookmark(project, job, bookmark);

        bookmark = createBookmark(project, job, values);
        sut.saveBookmark(project, job, bookmark);

        List<Bookmark> bookmarks = getBookmarks(project, job);

        assertThat(bookmarks.size(), is(1));

        Bookmark savedBookmark = bookmarks.get(0);

        assertThat(savedBookmark.getProjectId(), is(project.getId().toCompatibleProjectId()));
        assertThat(savedBookmark.getJobId(), is(job.getCompatibleJobId()));
        assertThat(savedBookmark.getName(), is(bookmark.getName()));
    }

    private ArrayList<Bookmark> getBookmarks(Project project, Job<?> job) {
        return new ArrayList<>(sut.getBookmarks(project, job).values());
    }

    @Test
    public void assert_that_when_no_bookmarks_are_registered_for_a_job_then_existsBookmark_retuns_false() throws Exception {
        Project project = createProject();
        Job<?> job = createJob("jobId");

        assertThat(sut.existsBookmark(project, job, "test"), is(false));
    }

    @Test
    public void assert_that_when_you_save_a_bookmark_then_existsBookmark_returns_true() throws Exception {
        Project project = createProject();
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark = createBookmark(project, job, values);
        sut.saveBookmark(project, job, bookmark);

        assertThat(sut.existsBookmark(project, job, bookmark.getName()), is(true));
    }

    @Test
    public void assert_that_when_no_bookmarks_are_registered_for_a_job_then_deleteBookmark_retuns_false() throws Exception {
        Project project = createProject();
        Job<?> job = createJob("jobId");

        assertThat(sut.deleteBookmark(project, job, "test"), is(false));
    }

    @Test
    public void assert_that_when_you_save_a_bookmark_then_deleteBookmark_returns_true() throws Exception {
        Project project = createProject();
        Job<?> job = createJob("jobId");

        JobValues values = mock(JobValues.class);
        Bookmark bookmark = createBookmark(project, job, values);
        sut.saveBookmark(project, job, bookmark);

        assertThat(sut.deleteBookmark(project, job, bookmark.getName()), is(true));
        assertThat(sut.getBookmarks(project, job).isEmpty(), is(true));
    }

    private static Project createProject() {
        try {
            return TestUtils.createProject("test:projectId");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Bookmark createBookmark(Project project, Job<?> job, JobValues values) {
        return createBookmark(project, job, values, "bookmark");
    }

    private Bookmark createBookmark(Project project, Job<?> job, JobValues values, String name) {
        String key = UUID.randomUUID().toString();
        return new Bookmark(project, job, key,name, values, null);
    }

}