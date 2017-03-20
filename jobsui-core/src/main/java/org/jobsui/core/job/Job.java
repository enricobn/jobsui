package org.jobsui.core.job;

import org.jobsui.core.JobFuture;
import org.jobsui.core.JobValues;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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
        for (JobDependency jobDependency : getUnsortedDependencies()) {
            groovyValues.put(jobDependency.getKey(), values.getValue(jobDependency));
        }
        return run(groovyValues);
    }

    List<String> validate(Map<String, Serializable> values);

    JobParameterDef getParameter(String key);

    JobExpression getExpression(String key);

    default ClassLoader getClassLoader() {
        return null;
    }

    default List<JobDependency> getUnsortedDependencies() {
        List<JobDependency> jobDependencies = new ArrayList<>(getParameterDefs());
        jobDependencies.addAll(getExpressions());
        return jobDependencies;
    }

    default List<JobDependency> getSortedDependencies() throws Exception {
        List<JobDependency> jobDependencies = getUnsortedDependencies();

        List<String> sortedDependencies = JobDependency.getSortedDependenciesKeys(jobDependencies);

        return jobDependencies.stream().sorted((o1, o2) -> {
            int i1 = sortedDependencies.indexOf(o1.getKey());
            int i2 = sortedDependencies.indexOf(o2.getKey());
            return i1 -i2;
        }).collect(Collectors.toList());
    }

}
