package org.jobsui.core.groovy;

import org.jobsui.core.JobParameterDef;

import java.io.Serializable;

/**
 * Created by enrico on 10/6/16.
 */
public interface JobParameterDefGroovy<T extends Serializable> extends JobParameterDef<T> {

    void addDependency(String key);

    void init(ProjectGroovy project);

}
