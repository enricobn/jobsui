package org.jobsui.core.runner;

import org.jobsui.core.job.Project;
import org.jobsui.core.job.Job;

import java.io.Serializable;

/**
 * Created by enrico on 3/21/17.
 */
public interface JobRunner {

    <T extends Serializable> T run(Project project, Job<T> job) throws Exception;

}
