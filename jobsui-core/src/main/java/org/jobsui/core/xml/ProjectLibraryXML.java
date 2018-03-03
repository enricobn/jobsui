package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectLibraryXML implements ValidatingXML {
    private String groupId;
    private String artifactId;
    private String version;

    public static ProjectLibraryXML of(String library) throws Exception {
        ProjectLibraryXML projectLibraryXML = new ProjectLibraryXML();
        String[] split = library.split(":");
        if (split.length != 3) {
            throw new Exception("Invalid format.");
        }

        projectLibraryXML.setGroupId(split[0]);
        projectLibraryXML.setArtifactId(split[1]);
        projectLibraryXML.setVersion(split[2]);

        return projectLibraryXML;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectLibraryXML that = (ProjectLibraryXML) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public List<String> validate() {
        List<String> messages = new ArrayList<>();

        if (isEmpty(groupId)) {
            messages.add("Group id is mandatory.");
        }

        if (isEmpty(artifactId)) {
            messages.add("Artifact id is mandatory.");
        }

        if (isEmpty(version)) {
            messages.add("Version id is mandatory.");
        }

        return messages;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
