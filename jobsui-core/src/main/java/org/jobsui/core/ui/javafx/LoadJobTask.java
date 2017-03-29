package org.jobsui.core.ui.javafx;

import javafx.concurrent.Task;
import org.jobsui.core.Project;
import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.jobsui.core.xml.ProjectXML;

import java.io.Serializable;

/**
 * Created by enrico on 3/30/17.
 */
class LoadJobTask extends Task<Job<Serializable>> {
    private final String projectFolder;
    private final String jobId;

    LoadJobTask(String projectFolder, String jobId) {
        this.projectFolder = projectFolder;
        this.jobId = jobId;
    }

    @Override
    protected Job<Serializable> call() throws Exception {
        ProjectXML projectXML;
        try {
            projectXML = JobParser.getParser(projectFolder).parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Project project;
        try {
            project = new ProjectGroovyBuilder().build(projectXML);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final Job<Serializable> job = project.getJob(jobId);

        if (job == null) {
            throw new RuntimeException("Cannot find project with id \"" + jobId + "\" in folder " +
                    projectFolder + " .");
        }
        return job;
    }
}
