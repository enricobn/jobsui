package org.jobsui.core.runner;

import org.jobsui.core.job.JobDependency;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 2/24/17.
 */
public class JobValuesImpl implements JobValues {
    private final Map<String, Serializable> values;

    public JobValuesImpl() {
        this.values =  new HashMap<>();
    }

    public JobValuesImpl(Map<String, Serializable> values) {
        this.values = values;
    }

    @Override
    public void setValue(JobDependency jobDependency, Serializable value) {
        values.put(jobDependency.getKey(), value);
    }

    @Override
    public Serializable getValue(JobDependency jobDependency) {
        return values.get(jobDependency.getKey());
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        values.entrySet().forEach(e -> stringBuilder.append(e.getKey())
                .append("=")
                .append(e.getValue())
                .append("\n")
        );
        return stringBuilder.toString();
    }
}
