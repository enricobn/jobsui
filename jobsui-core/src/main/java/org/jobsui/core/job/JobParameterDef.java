package org.jobsui.core.job;

import org.jobsui.core.ParameterDefUI;
import org.jobsui.core.ParameterValidator;

import java.io.Serializable;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobParameterDef extends JobDependency, ParameterValidator, ParameterDefUI {

    String getName();

    boolean isOptional();

    Serializable getDefaultValue();

    //TODO to be removed: I must create a JobCall, now there's a weird implementation for groovy (JobCallDefGroovy)
    default boolean isCalculated() {
        return false;
    }

}
