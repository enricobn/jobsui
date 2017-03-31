package org.jobsui.core.ui.javafx;

import javafx.concurrent.Task;
import org.jobsui.core.Project;
import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.jobsui.core.xml.JobXML;
import org.jobsui.core.xml.ProjectXML;

import java.io.Serializable;

/**
 * Created by enrico on 3/30/17.
 */
class LoadProjectXMLTask extends Task<ProjectXML> {
    private final String projectFolder;

    LoadProjectXMLTask(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    @Override
    protected ProjectXML call() throws Exception {
        ProjectXML projectXML;
        try {
            projectXML = JobParser.getParser(projectFolder).parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return projectXML;
    }
}
