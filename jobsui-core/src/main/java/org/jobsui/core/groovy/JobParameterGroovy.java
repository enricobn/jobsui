package org.jobsui.core.groovy;

import org.jobsui.core.job.JobParameter;

/**
 * Created by enrico on 10/6/16.
 */
public interface JobParameterGroovy extends JobDependencyGroovy, JobParameter {

    void init(ProjectGroovy project);

}
