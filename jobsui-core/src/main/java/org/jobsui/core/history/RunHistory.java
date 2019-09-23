package org.jobsui.core.history;

import org.jobsui.core.job.*;
import org.jobsui.core.jobstore.JobStoreElementImpl;
import org.jobsui.core.runner.JobValues;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class RunHistory extends JobStoreElementImpl {
    private final CompatibleProjectId projectId;
    private final CompatibleJobId jobId;

    private final LocalDateTime dateTime;
    private final Map<String, Serializable> values = new HashMap<>();

    public RunHistory(Project project, Job<?> job, String key, LocalDateTime dateTime, JobValues values) {
        super(key);
        this.projectId = project.getId().toCompatibleProjectId();
        this.jobId = job.getCompatibleJobId();
        this.dateTime = dateTime;
        for (JobParameter parameterDef : job.getParameterDefs()) {
            if (!parameterDef.isCalculated()) {
                this.values.put(parameterDef.getKey(), values.getValue(parameterDef));
            }
        }
    }

    public LocalDateTime getDateTime() {
        return dateTime;
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

}
