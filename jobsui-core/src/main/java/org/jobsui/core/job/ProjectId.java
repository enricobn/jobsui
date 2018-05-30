package org.jobsui.core.job;

import com.github.zafarkhaja.semver.Version;

import java.util.Objects;

/**
 * Created by enrico on 5/4/17.
 */
public class ProjectId {
    private final String groupId;
    private final String artifactId;
    private final Version version;

    public static ProjectId of(String id, Version version) throws Exception {
        String[] components = id.split(":");
        if (components.length != 2) {
            throw new Exception(String.format("Invalid number of components: %s.", components.length));
        }
        return new ProjectId(components[0], components[1], version);
    }

    public static ProjectId of(String id, String version) throws Exception {
        return of(id, Version.valueOf(version));
    }

    public ProjectId(String groupId, String artifactId, Version version) {
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(artifactId);
        Objects.requireNonNull(version);
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public Version getVersion() {
        return version;
    }

    public CompatibleProjectId toCompatibleProjectId() {
        return new CompatibleProjectId(groupId, artifactId, version.getMajorVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectId projectId = (ProjectId) o;

        if (!groupId.equals(projectId.groupId)) return false;
        if (!artifactId.equals(projectId.artifactId)) return false;
        return version.equals(projectId.version);
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
