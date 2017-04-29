package org.jobsui.core.runner;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.Serializable;

/**
 * Created by enrico on 3/21/17.
 */
interface JobRunner {

    <T extends Serializable> T run(Project project, Job<T> job) throws Exception;

}
