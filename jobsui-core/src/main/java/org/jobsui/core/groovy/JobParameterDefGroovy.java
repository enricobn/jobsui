package org.jobsui.core.groovy;

import org.jobsui.core.JobParameterDef;

/**
 * Created by enrico on 10/6/16.
 */
public interface JobParameterDefGroovy extends JobDependencyGroovy, JobParameterDef {

    void init(ProjectGroovy project);

}
