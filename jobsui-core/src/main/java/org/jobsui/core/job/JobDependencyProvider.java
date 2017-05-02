package org.jobsui.core.job;

/**
 * Created by enrico on 5/2/17.
 */
public interface JobDependencyProvider {

    JobDependency getJobDependency(String key);

}
