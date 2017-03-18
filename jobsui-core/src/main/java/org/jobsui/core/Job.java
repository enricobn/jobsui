package org.jobsui.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/29/16.
 */
public interface Job<T> {

    String getId();

    String getName();

    List<JobParameterDef> getParameterDefs();

    List<JobExpression> getExpressions();

    JobFuture<T> run(Map<String,Serializable> values);

    default JobFuture<T> run(JobValues values) {
        Map<String, Serializable> groovyValues = new HashMap<>();
        for (JobParameterDef parameterDef : getParameterDefs()) {
            groovyValues.put(parameterDef.getKey(), values.getValue(parameterDef));
        }
        return run(groovyValues);
    }

    List<String> validate(Map<String, Serializable> values);

    JobParameterDef getParameter(String key);

    default ClassLoader getClassLoader() {
        return null;
    }
}
