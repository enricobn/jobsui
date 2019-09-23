package org.jobsui.core.jobstore;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.IOException;
import java.util.List;

/**
 * Used to save and restore a Bean related to a Job
 * @param <T>
 */
public interface JobStore<T extends JobStoreElement> {

    void save(Project project, Job job, T value) throws IOException;

    List<T> get(Project project, Job job);

    boolean delete(Project project, Job job, T value);

}
