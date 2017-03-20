package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobDependency;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 2/24/17.
 */
public interface JobValues {

    void setValue(JobDependency jobDependency, Serializable value);

    Serializable getValue(JobDependency jobDependency);

    void clear();

    default Map<String, Serializable> getMap(Job<?> job) {
        Map<String, Serializable> result = new HashMap<>();
        for (JobDependency jobDependency : job.getUnsortedDependencies()) {
            result.put(jobDependency.getKey(), getValue(jobDependency));
        }
        return result;
    }
}
