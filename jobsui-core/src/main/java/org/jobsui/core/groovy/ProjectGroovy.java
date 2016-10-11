package org.jobsui.core.groovy;

import org.jobsui.core.Job;
import org.jobsui.core.Project;
import org.jobsui.core.xml.ProjectXML;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectGroovy implements Project {
    private final ProjectXML projectXML;
    private final Map<String, JobGroovy<?>> jobs;
    private final Map<String, Project> projects = new HashMap<>();

    public ProjectGroovy(File projectFolder, ProjectXML projectXML, Map<String, JobGroovy<?>> jobs) {
        this.projectXML = projectXML;
        this.jobs = jobs;
        getProjectXML().getImports().entrySet().stream().forEach(entry -> {
            JobParser jobParser;
            try {
                jobParser = new JobParser();
                projects.put(entry.getKey(), jobParser.loadProject(new File(projectFolder, entry.getValue())));
            } catch (Exception e) {
                // TODO
                throw new RuntimeException(e);
            }
        });
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

    public Collection<File> getGroovyFiles() {
        return projectXML.getGroovyFiles();
    }

    public Project getProject(String key) {
        return projects.get(key);
    }
}
