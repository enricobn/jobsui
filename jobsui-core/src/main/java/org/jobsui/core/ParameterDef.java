package org.jobsui.core;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 2/18/17.
 */
public interface ParameterDef<T extends Serializable> extends ParameterValidator<T> {

    String getKey();

    String getName();

    boolean isOptional();

    T getDefaultValue();

    List<JobParameterDef<? extends Serializable>> getDependencies();

    default boolean isCalculated() {
        return false;
    }

}
