package org.jobsui.core.job;

import java.util.*;

import static org.jobsui.core.utils.JobsUIUtils.repl;

/**
 * Created by enrico on 5/2/17.
 */
public class JobDependencyTree {
    private static final int INDENT = 2;
    private final Map<String, JobDependency> dependencies;
    private final List<JobDependency> sortedDependencies;

    public JobDependencyTree(Collection<JobDependency> dependencies) throws Exception {
        this.dependencies = new HashMap<>();
        dependencies.forEach(dependency -> this.dependencies.put(dependency.getKey(), dependency));
        this.sortedDependencies = JobDependency.sort(dependencies);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (JobDependency jobDependency : sortedDependencies) {
            toString(builder, jobDependency, 0);
        }
        return builder.toString();
    }

    private void toString(StringBuilder builder, JobDependency jobDependency, int indent) {
        builder.append(repl(" ", indent * INDENT));
        builder.append(jobDependency.getName()).append(System.lineSeparator());
        for (String key : jobDependency.getDependencies()) {
            toString(builder, dependencies.get(key), indent + 1);
        }
    }
}
