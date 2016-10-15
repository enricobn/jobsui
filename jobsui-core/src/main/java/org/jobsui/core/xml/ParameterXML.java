package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public class ParameterXML {
    private final List<String> dependencies = new ArrayList<>();
    private int order;
    private String key;
    private String name;

    ParameterXML(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public void addDependency(String depKey) {
        dependencies.add(depKey);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    int getOrder() {
        return order;
    }

    public boolean isOptional() {
        return false;
    }

    public boolean isVisible() {
        return true;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean removeDependency(String key) {
        return dependencies.remove(key);
    }

    public void setOrder(int order) {
        this.order = order;
    }
}