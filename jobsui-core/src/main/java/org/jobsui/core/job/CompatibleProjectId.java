package org.jobsui.core.job;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by enrico on 5/4/17.
 */
public class CompatibleProjectId implements Serializable {
    private final String groupId;
    private final String moduleId;
    private final int majorVersion;

    public CompatibleProjectId(String groupId, String moduleId, int majorVersion) {
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(moduleId);
        this.groupId = groupId;
        this.moduleId = moduleId;
        this.majorVersion = majorVersion;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompatibleProjectId that = (CompatibleProjectId) o;

        if (majorVersion != that.majorVersion) return false;
        if (!groupId.equals(that.groupId)) return false;
        return moduleId.equals(that.moduleId);
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + moduleId.hashCode();
        result = 31 * result + majorVersion;
        return result;
    }

    @Override
    public String toString() {
        return groupId + ":" + moduleId + ":" + majorVersion;
    }
}
