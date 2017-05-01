package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/17.
 */
public class WizardStepImpl implements WizardStep {
    private List<String> dependencies = new ArrayList<>();
    private String validateScript;
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public String getValidateScript() {
        return validateScript;
    }

    public void setValidateScript(String validateScript) {
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

        WizardStepImpl that = (WizardStepImpl) o;

        if (!dependencies.equals(that.dependencies)) return false;
        if (validateScript != null ? !validateScript.equals(that.validateScript) : that.validateScript != null)
            return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = dependencies.hashCode();
        result = 31 * result + (validateScript != null ? validateScript.hashCode() : 0);
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
