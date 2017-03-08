package org.jobsui.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 2/24/17.
 */
public interface JobValues {

    <V extends Serializable> void setValue(ParameterDef<V> parameterDef, V value);

    <V extends Serializable> V getValue(ParameterDef<V> parameterDef);

    void clear();

    default Map<String, Serializable> getMap(Job<?> job) {
        Map<String, Serializable> result = new HashMap<>();
        for (JobParameterDef<? extends Serializable> parameterDef : job.getParameterDefs()) {
            result.put(parameterDef.getKey(), getValue(parameterDef));
        }
        return result;
    }
}
