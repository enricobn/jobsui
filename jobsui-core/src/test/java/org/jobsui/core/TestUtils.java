package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 4/17/17.
 */
public interface TestUtils {

    static Job<?> createJob(String jobId) {
        Job<?> job = mock(Job.class);
        when(job.getId()).thenReturn(jobId);
        return job;
    }

    static Project createProject(String projectId) {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        return project;
    }

}
