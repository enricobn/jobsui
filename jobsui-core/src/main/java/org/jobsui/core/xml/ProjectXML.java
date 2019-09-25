package org.jobsui.core.xml;

import org.jobsui.core.ui.UIComponentRegistry;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 4/5/17.
 */
public interface ProjectXML extends ValidatingXML {

    URL getRelativeURL(String relativePath);

    Set<ProjectLibraryXML> getLibraries();

    Map<String, String> getImports();

    String getName();

    String getId();

    List<String> getScriptsLocations();

    Collection<JobXML> getJobs();

    String getVersion();

    default URL[] getScripsLocationsURLS() {
        return getScriptsLocations().stream()
                .map(location -> getRelativeURL(location + "/"))
                .toArray(URL[]::new);
    }

    UIComponentRegistry getUiComponentRegistry();

    void addJob(JobXML jobXML);

    JobXML getJobXMLById(String job);

}
