package org.jobsui.core.xml;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by enrico on 4/30/17.
 */
public class JobPageImpl implements JobPage {
    private final Set<String> dependencies = new HashSet<>();
    private String validateScript;
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getDependencies() {
        return dependencies;
    }

    @Override
    public String getValidateScript() {
        return validateScript;
    }

    void setValidateScript(String validateScript) {
        this.validateScript = validateScript;
    }

    public void addDependency(String dependency) {
        dependencies.add(dependency);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobPageImpl that = (JobPageImpl) o;
        return Objects.equals(dependencies, that.dependencies) &&
                Objects.equals(validateScript, that.validateScript) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencies, validateScript, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
