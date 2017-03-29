package org.jobsui.core;

import java.util.List;

/**
 * Created by enrico on 3/29/17.
 */
public interface JobsUIPreferences {

    List<String> getLastOpenedProjects();

    void registerOpenedProject(String project);

}
