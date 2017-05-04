package org.jobsui.core.job;

import java.util.Objects;

/**
 * Created by enrico on 5/4/17.
 */
public class CompatibleJobId {
    private final String id;
    private final int majorVersion;

    public CompatibleJobId(String id, int majorVersion) {
        Objects.requireNonNull(id);
        this.id = id;
        this.majorVersion = majorVersion;
    }

    public String getId() {
        return id;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompatibleJobId that = (CompatibleJobId) o;

        if (majorVersion != that.majorVersion) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + majorVersion;
        return result;
    }

    @Override
    public String toString() {
        return id + ":" + majorVersion;
    }
}
