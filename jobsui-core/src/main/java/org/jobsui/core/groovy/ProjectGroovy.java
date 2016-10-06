package org.jobsui.core.groovy;

import org.jobsui.core.Job;
import org.jobsui.core.Project;

import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectGroovy implements Project {
    private final ProjectXML projectXML;
    private final Map<String, JobGroovy<?>> jobs;

    public ProjectGroovy(ProjectXML projectXML, Map<String, JobGroovy<?>> jobs) {
        this.projectXML = projectXML;
        this.jobs = jobs;
    }

    @Override
    public <T> Job<T> getJob(String key) {
        return (Job<T>) jobs.get(key);
    }

    @Override
    public Set<String> getKeys() {
        return jobs.keySet();
    }

    public ProjectXML getProjectXML() {
        return projectXML;
    }
}
