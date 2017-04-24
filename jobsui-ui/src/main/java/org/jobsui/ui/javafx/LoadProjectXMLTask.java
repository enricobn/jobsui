package org.jobsui.ui.javafx;

import javafx.concurrent.Task;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectFSXML;

import java.io.File;
import java.net.URL;

/**
 * Created by enrico on 3/30/17.
 */
class LoadProjectXMLTask extends Task<ProjectFSXML> {
    private final File projectFolder;

    LoadProjectXMLTask(File projectFolder) {
        this.projectFolder = projectFolder;
    }

    @Override
    protected ProjectFSXML call() throws Exception {
        ProjectFSXML projectXML;
        try {
            projectXML = new ProjectParserImpl().parse(projectFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return projectXML;
    }
}
