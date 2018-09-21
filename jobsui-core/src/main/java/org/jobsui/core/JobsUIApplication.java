package org.jobsui.core;

import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.xml.ProjectFSXML;

import java.io.Serializable;

public interface JobsUIApplication {
    void gotoMain();

    void gotoRun(Project project, Job<Serializable> job);

    void gotoNew();

    void gotoEdit(ProjectFSXML projectXML);
}
