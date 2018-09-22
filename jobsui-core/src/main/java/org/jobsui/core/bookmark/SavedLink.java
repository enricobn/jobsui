package org.jobsui.core.bookmark;

import java.io.Serializable;
import java.util.Objects;

public class SavedLink implements Serializable {
    private final String id;
    private final String jobId;
    private final String name;

    public SavedLink(String id, String jobId, String name) {
        this.id = id;
        this.jobId = jobId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getJobId() {
        return jobId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedLink savedLink = (SavedLink) o;
        return Objects.equals(id, savedLink.id) &&
                Objects.equals(jobId, savedLink.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobId);
    }
}
