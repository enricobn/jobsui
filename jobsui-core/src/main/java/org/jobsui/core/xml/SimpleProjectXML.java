package org.jobsui.core.xml;

import org.jobsui.core.ui.UIComponentRegistry;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 4/20/17.
 */
public interface SimpleProjectXML extends ValidatingXML {

    URL getRelativeURL(String relativePath);

    Set<ProjectLibraryXML> getLibraries();

    Map<String, String> getImports();

    String getName();

    String getId();

    List<String> getScriptsLocations();

    Collection<String> getJobs();

    String getVersion();

    default String getJobId(String job) {
        int pos = job.lastIndexOf('.');
        return job.substring(0, pos);
    }

    default URL[] getScripsLocationsURLS() {
        return getScriptsLocations().stream()
                .map(location -> getRelativeURL(location + "/"))
                .toArray(URL[]::new);
    }

    UIComponentRegistry getUiComponentRegistry();

}
