package org.jobsui.core.xml;

import org.jobsui.core.groovy.ProjectParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by enrico on 4/5/17.
 */
public interface ProjectXML extends ValidatingXML {

    URL getRelativeURL(String relativePath);

    Set<String> getLibraries();

    Map<String, String> getImports();

    String getName();

    String getId();

    URL[] getScriptsURLS() throws MalformedURLException;

    Collection<String> getJobs();

    default String getJobId(String job) {
        int pos = job.lastIndexOf('.');
        return job.substring(0, pos);
    }

}
