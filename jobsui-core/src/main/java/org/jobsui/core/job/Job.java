package org.jobsui.core.job;

import com.github.zafarkhaja.semver.Version;
import org.jobsui.core.runner.JobResult;
import org.jobsui.core.runner.JobValues;
import org.jobsui.core.xml.JobPage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/29/16.
 */
public interface Job<T> extends JobDependencyProvider {

    String getId();

    Version getVersion();

    String getName();

    List<JobParameter> getParameterDefs();

    List<JobExpression> getExpressions();

    JobResult<T> run(Map<String,Serializable> values);

    default JobResult<T> run(JobValues values) {
        Map<String, Serializable> groovyValues = new HashMap<>();
        for (JobDependency jobDependency : getUnsortedDependencies()) {
            groovyValues.put(jobDependency.getKey(), values.getValue(jobDependency));
        }
        return run(groovyValues);
    }

    List<String> validate(Map<String, Serializable> values);

    List<JobPage> getJobPages();

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

    default JobDependency getJobDependency(String key) {
        JobDependency jobDependency = getParameter(key);
        if (jobDependency == null) {
            return getExpression(key);
        } else {
            return jobDependency;
        }
    }

    default CompatibleJobId getCompatibleJobId() {
        return new CompatibleJobId(getId(), getVersion().getMajorVersion());
    }


    default JobParameter getParameter(String key) {
        for (JobParameter parameterDef : getParameterDefs()) {
            if (parameterDef.getKey().equals(key)) {
                return parameterDef;
            }
        }
        return null;
    }

    default JobExpression getExpression(String key) {
        for (JobExpression jobExpression : getExpressions()) {
            if (jobExpression.getKey().equals(key)) {
                return jobExpression;
            }
        }
        return null;
    }

}
