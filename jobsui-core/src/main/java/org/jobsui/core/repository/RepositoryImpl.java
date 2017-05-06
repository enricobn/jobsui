package org.jobsui.core.repository;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.ProjectId;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by enrico on 5/4/17.
 */
public class RepositoryImpl implements Repository {
    private final URL root;
    private Set<ProjectId> projectIds;

    public RepositoryImpl(URL root) {
        this.root = root;
    }

    @Override
    public Optional<Project> getProject(String id, Version version) throws Exception {
        ProjectId projectId = ProjectId.of(id, version);

        if (!getProjectIds().contains(projectId)) {
            return Optional.empty();
        }

        URL url = getUrl(projectId);
        ProjectXML projectXML = new ProjectParserImpl().parse(url);

        return Optional.of(new ProjectGroovyBuilder().build(projectXML));
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return new URL(root.getProtocol() + ":" + root.getPath() + "/" + url.getPath()).openConnection();
    }

    private URL getUrl(ProjectId projectId) throws MalformedURLException {
        return new URL(root.toString() + "/" + projectId.getGroupId() + "/" + projectId.getModuleId() + "/" + projectId.getVersion().toString());
    }

    private synchronized Set<ProjectId> getProjectIds() throws IOException {
        if (projectIds == null) {
            URL projectsURL = new URL(root.toExternalForm() + "/projects.txt");
            try (InputStream is = projectsURL.openStream()) {
                projectIds = IOUtils.readLines(is, Charset.forName("UTF-8")).stream()
                        .map(this::fromString)
                        .collect(Collectors.toSet());
            }
        }
        return projectIds;
    }

    private ProjectId fromString(String s) {
        String[] strings = s.split(":");
        return new ProjectId(strings[0], strings[1], Version.valueOf(strings[2]));
    }
}
