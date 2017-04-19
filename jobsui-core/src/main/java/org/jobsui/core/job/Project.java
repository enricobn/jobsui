package org.jobsui.core.job;

import org.jobsui.core.job.Job;

import java.util.Set;

/**
 * Created by enrico on 5/6/16.
 */
public interface Project {

    String getId();

    <T> Job<T> getJob(String key);

    Set<String> getJobsIds();

    String getName();
}
