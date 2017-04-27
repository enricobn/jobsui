package org.jobsui.core.bookmark;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.JobParameter;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobValues;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 3/6/17.
 */
public class Bookmark {
    private final String projectId;
    private final String jobId;
    private final String name;
    private final Map<String, Serializable> values = new HashMap<>();

    public Bookmark(Project project, Job<?> job, String name, JobValues values) {
        this.projectId = project.getId();
        this.jobId = job.getId();
        this.name = name;
        for (JobParameter parameterDef : job.getParameterDefs()) {
            if (!parameterDef.isCalculated()) {
                this.values.put(parameterDef.getKey(), values.getValue(parameterDef));
            }
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, Serializable> getValues() {
        return values;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getJobId() {
        return jobId;
    }

    @Override
    public String toString() {
        return getName();
    }
}
