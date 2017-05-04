package org.jobsui.core;

import com.github.zafarkhaja.semver.Version;
import org.jobsui.core.job.CompatibleJobId;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.ProjectId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 4/17/17.
 */
public interface TestUtils {

    static Job<?> createJob(String jobId) {
        Job<?> job = mock(Job.class);
        when(job.getId()).thenReturn(jobId);
        when(job.getVersion()).thenReturn(Version.valueOf("1.0.0"));
        when(job.getCompatibleJobId()).thenReturn(new CompatibleJobId(jobId, 1));
        return job;
    }

    static Project createProject(String projectId) throws Exception {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(ProjectId.of(projectId, "1.0.0"));
        return project;
    }

}
