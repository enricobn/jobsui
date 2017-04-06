package org.jobsui.core.ui.javafx;

import javafx.concurrent.Task;
import org.jobsui.core.groovy.ProjectParserImpl;
import org.jobsui.core.xml.ProjectFSXMLImpl;

import java.net.URL;

/**
 * Created by enrico on 3/30/17.
 */
class LoadProjectXMLTask extends Task<ProjectFSXMLImpl> {
    private final String projectFolder;

    LoadProjectXMLTask(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    @Override
    protected ProjectFSXMLImpl call() throws Exception {
        ProjectFSXMLImpl projectXML;
        try {
            projectXML = new ProjectParserImpl().parse(new URL(projectFolder));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return projectXML;
    }
}
