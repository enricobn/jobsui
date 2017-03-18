package org.jobsui.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 2/24/17.
 */
public interface JobValues {

    void setValue(ParameterDef parameterDef, Serializable value);

    Serializable getValue(ParameterDef parameterDef);

    void clear();

    default Map<String, Serializable> getMap(Job<?> job) {
        Map<String, Serializable> result = new HashMap<>();
        for (JobParameterDef parameterDef : job.getParameterDefs()) {
            result.put(parameterDef.getKey(), getValue(parameterDef));
        }
        return result;
    }
}
