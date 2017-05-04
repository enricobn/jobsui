package org.jobsui.core.bookmark;

import org.jobsui.core.job.*;
import org.jobsui.core.runner.JobValues;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 3/6/17.
 */
public class Bookmark {
    private final CompatibleProjectId projectId;
    private final CompatibleJobId jobId;
    private final String name;
    private final Map<String, Serializable> values = new HashMap<>();

    public Bookmark(Project project, Job<?> job, String name, JobValues values) {
        this.projectId = project.getId().toCompatibleProjectId();
        this.jobId = job.getCompatibleJobId();
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

    public CompatibleProjectId getProjectId() {
        return projectId;
    }

    public CompatibleJobId getJobId() {
        return jobId;
    }

    @Override
    public String toString() {
        return getName();
    }
}
