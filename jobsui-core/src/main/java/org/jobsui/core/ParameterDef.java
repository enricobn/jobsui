package org.jobsui.core;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 2/18/17.
 */
public interface ParameterDef extends ParameterValidator {

    String getKey();

    String getName();

    boolean isOptional();

    Serializable getDefaultValue();

    List<String> getDependencies();

    default boolean isCalculated() {
        return false;
    }

}
