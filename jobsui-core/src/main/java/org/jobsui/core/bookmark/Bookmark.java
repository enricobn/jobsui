package org.jobsui.core.bookmark;

import org.jobsui.core.job.*;
import org.jobsui.core.runner.JobValues;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by enrico on 3/6/17.
 */
public class Bookmark implements Serializable {
    private final CompatibleProjectId projectId;
    private final CompatibleJobId jobId;
    private final String key;
    private final String name;
    private final Map<String, Serializable> values = new HashMap<>();
    private String tags;

    public Bookmark(Project project, Job<?> job, String key, String name, JobValues values, String tags) {
        this.projectId = project.getId().toCompatibleProjectId();
        this.jobId = job.getCompatibleJobId();
        this.key = key;
        this.name = name;
        this.tags = tags;
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

    public String getKey() {
        return key;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return getName();
    }
}
