package org.jobsui.core.groovy;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectGroovy implements Project {
    private final String id;
    private final String name;
    private final Map<String, JobGroovy<Serializable>> jobs;
    private final Map<String, Project> referencedProjects;

    public ProjectGroovy(String id, String name, Map<String, JobGroovy<Serializable>> jobs, Map<String, Project> referencedProjects) {
        this.id = id;
        this.name = name;
        this.jobs = jobs;
        this.referencedProjects = referencedProjects;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public <T> Job<T> getJob(String id) {
        return (Job<T>) jobs.get(id);
    }

    @Override
    public Set<String> getJobsIds() {
        return jobs.keySet();
    }

    @Override
    public String getName() {
        return name;
    }

    public Project getProject(String id) {
        return referencedProjects.get(id);
    }
}
