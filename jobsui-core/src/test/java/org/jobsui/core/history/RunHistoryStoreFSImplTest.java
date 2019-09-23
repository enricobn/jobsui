package org.jobsui.core.history;

import org.jobsui.core.TestUtils;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.jobstore.JobStoreElementImpl;
import org.jobsui.core.runner.JobValues;
import org.jobsui.core.utils.JobsUIUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class RunHistoryStoreFSImplTest {
    private RunHistoryStoreFSImpl sut;

    @Before
    public void setUp() throws IOException {
        File root = JobsUIUtils.createTempDir("runHistory", "test");
        sut = new RunHistoryStoreFSImpl(root);
    }

    @Test
    public void no_more_than_max_elements_are_saved_and_last_added() throws Exception {
        Project project = TestUtils.createProject("test:projectId");
        Job<?> job = TestUtils.createJob("testJob");

        String lastKey = null;
        for (int i = 0; i < RunHistoryStoreFSImpl.MAX_ELEMENTS + 1; i++) {
            JobValues jobValues = mock(JobValues.class);

            lastKey = "KEY" + i;
            LocalDateTime time = LocalDateTime.now().plus(i, ChronoUnit.SECONDS);
            RunHistory runHistory = new RunHistory(project, job, lastKey, time, jobValues);

            sut.save(project, job, runHistory);
        }

        assertThat(sut.get(project, job).size(), is(RunHistoryStoreFSImpl.MAX_ELEMENTS));

        assertThat(sut.getLast(project, job).map(JobStoreElementImpl::getKey), is(Optional.ofNullable(lastKey)));
    }
}