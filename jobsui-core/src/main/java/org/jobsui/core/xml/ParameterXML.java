package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public class ParameterXML {
    private final String key;
    private final String name;
    private final List<String> dependencies = new ArrayList<>();
    private final int order;

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
}
