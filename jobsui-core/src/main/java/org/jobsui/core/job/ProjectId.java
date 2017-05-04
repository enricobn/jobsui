package org.jobsui.core.job;

import com.github.zafarkhaja.semver.Version;

import java.util.Objects;

/**
 * Created by enrico on 5/4/17.
 */
public class ProjectId {
    private final String groupId;
    private final String moduleId;
    private final Version version;

    public static ProjectId of(String id, String version) throws Exception {
        String[] components = id.split(":");
        if (components.length != 2) {
            throw new Exception(String.format("Invalid number of components: %s.", components.length));
        }
        return new ProjectId(components[0], components[1], Version.valueOf(version));
    }

    public ProjectId(String groupId, String moduleId, Version version) {
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(moduleId);
        Objects.requireNonNull(version);
        this.groupId = groupId;
        this.moduleId = moduleId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public Version getVersion() {
        return version;
    }

    public CompatibleProjectId toCompatibleProjectId() {
        return new CompatibleProjectId(groupId, moduleId, version.getMajorVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectId projectId = (ProjectId) o;

        if (!groupId.equals(projectId.groupId)) return false;
        if (!moduleId.equals(projectId.moduleId)) return false;
        return version.equals(projectId.version);
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + moduleId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return groupId + ":" + moduleId + ":" + version;
    }
}
