package org.jobsui.core.jobstore;

public class JobStoreElementImpl implements JobStoreElement {
    private final String key;

    public JobStoreElementImpl(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

}
