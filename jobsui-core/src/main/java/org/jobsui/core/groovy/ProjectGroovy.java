package org.jobsui.core.groovy;

import org.jobsui.core.Job;
import org.jobsui.core.Project;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectGroovy implements Project {
    private final ProjectXML projectXML;
    private final Map<String, JobGroovy<?>> jobs;
    private final Collection<File> groovyFiles;

    public ProjectGroovy(ProjectXML projectXML, Map<String, JobGroovy<?>> jobs, Collection<File> groovyFiles) {
        this.projectXML = projectXML;
        this.jobs = jobs;
        this.groovyFiles = groovyFiles;
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
        return projectXML.getName();
    }

    public ProjectXML getProjectXML() {
        return projectXML;
    }
}
