package org.jobsui.core.groovy;

import org.jobsui.core.job.JobDependency;

/**
 * Created by enrico on 10/6/16.
 */
interface JobDependencyGroovy extends JobDependency {

    void addDependency(String key);

}
