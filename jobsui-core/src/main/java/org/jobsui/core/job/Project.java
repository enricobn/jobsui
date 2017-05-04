package org.jobsui.core.job;

import java.util.Set;

/**
 * Created by enrico on 5/6/16.
 */
public interface Project {

    ProjectId getId();

    <T> Job<T> getJob(String key);

    Set<String> getJobsIds();

    String getName();
}
