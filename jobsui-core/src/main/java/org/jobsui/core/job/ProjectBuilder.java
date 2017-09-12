package org.jobsui.core.job;

import org.jobsui.core.job.Project;
import org.jobsui.core.xml.ProjectXML;

public interface ProjectBuilder {
    Project build(ProjectXML projectXML) throws Exception;
}
