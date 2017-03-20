package org.jobsui.core.xml;

import org.jobsui.core.JobDependency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public abstract class JobDependencyXML implements JobDependency {
    private final List<String> dependencies = new ArrayList<>();
    private int order;
    private String key;

    JobDependencyXML(String key) {
        this.key = key;
    }

    public void addDependency(String depKey) {
        dependencies.add(depKey);
    }

    public String getKey() {
        return key;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public boolean removeDependency(String key) {
        return dependencies.remove(key);
    }

    public void setKey(String key) {
        this.key = key;
    }

}