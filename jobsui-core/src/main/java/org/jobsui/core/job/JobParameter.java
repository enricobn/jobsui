package org.jobsui.core.job;

import org.jobsui.core.JobParameterUI;

import java.io.Serializable;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobParameter extends JobDependency, JobParameterValidator, JobParameterUI {

    String getName();

    boolean isOptional();

    Serializable getDefaultValue();

    //TODO to be removed: I must create a JobCall, now there's a weird implementation for groovy (JobCallDefGroovy)
    default boolean isCalculated() {
        return false;
    }

}
