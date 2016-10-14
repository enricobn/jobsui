package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public class ParameterXML {
    private final List<String> dependencies = new ArrayList<>();
    private final int order;
    private String key;
    private String name;

    ParameterXML(String key, String name, int order) {
        this.key = key;
        this.name = name;
        this.order = order;
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

    public void removeDependency(String key) {
        dependencies.remove(key);
    }
}
