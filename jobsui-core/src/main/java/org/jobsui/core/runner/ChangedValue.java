package org.jobsui.core.runner;

import org.jobsui.core.job.JobDependency;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/24/17.
 */
public class ChangedValue {
    public final JobDependency jobDependency;
    public final Map<String, Serializable> values;
    public final Map<String, Serializable> validValues;
    private final List<String> validation;

    public ChangedValue(JobDependency jobDependency, Map<String, Serializable> values, Map<String, Serializable> validValues,
                        List<String> validation) {
        this.jobDependency = jobDependency;
        this.values = values;
        this.validValues = validValues;
        this.validation = validation;
    }
}
