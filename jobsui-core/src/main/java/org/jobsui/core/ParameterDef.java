package org.jobsui.core;

import java.io.Serializable;

/**
 * Created by enrico on 2/18/17.
 */
public interface ParameterDef extends JobDependency,ParameterValidator {

    String getName();

    boolean isOptional();

    Serializable getDefaultValue();

    //TODO to be removed: I must create a JobCall, now there's a weird implementation for groovy (JobCallDefGroovy)
    default boolean isCalculated() {
        return false;
    }

}
