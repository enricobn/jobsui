package org.jobsui.core.groovy;

import org.jobsui.core.Job;
import org.jobsui.core.Project;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectGroovy implements Project {
    private final String name;
    private final Map<String, JobGroovy<Serializable>> jobs;
    private final Map<String, Project> referencedProjects;

    public ProjectGroovy(String name, Map<String, JobGroovy<Serializable>> jobs, Map<String, Project> referencedProjects) {
        this.name = name;
        this.jobs = jobs;
        this.referencedProjects = referencedProjects;
    }

    @Override
    public <T> Job<T> getJob(String key) {
        return (Job<T>) jobs.get(key);
    }

    @Override
    public Set<String> getKeys() {
        return jobs.keySet();
    }

    @Override
    public String getName() {
        return name;
    }

    public Project getProject(String key) {
        return referencedProjects.get(key);
    }
}
