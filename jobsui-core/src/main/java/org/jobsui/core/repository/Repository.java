package org.jobsui.core.repository;

import com.github.zafarkhaja.semver.Version;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.UI;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

/**
 * Created by enrico on 5/4/17.
 */
public interface Repository {

    Optional<Project> getProject(String id, Version version, BookmarksStore bookmarksStore, UI ui) throws Exception;

    URLConnection openConnection(URL url) throws IOException;
}
