package org.jobsui.core.repository;

import com.github.zafarkhaja.semver.Version;
import org.jobsui.core.job.Project;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

/**
 * Created by enrico on 5/4/17.
 */
public interface Repository {

    Optional<Project> getProject(String id, Version version) throws Exception;

    URLConnection openConnection(URL url) throws IOException;
}
